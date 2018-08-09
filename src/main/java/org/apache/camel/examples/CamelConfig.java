/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.camel.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.UnmarshalException;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.model.dataformat.JsonLibrary;

@ApplicationScoped
@ContextName("camel-context")
public class CamelConfig extends RouteBuilder {
  
  @Inject
  private AddressProcessor addressProcessor;
  
  @Inject
  private AuditMessageProcessor auditMessageProcessor;

  @Inject
  @ContextName("camel-context")
  private CamelContext camelContext;
  
  @PostConstruct
  private void initializePropertyPlaceholder() {
    PropertiesComponent properties = camelContext.getComponent("properties", PropertiesComponent.class);

    List<String> propertyLocations = new ArrayList<>();
    propertyLocations.add("classpath:/application.properties;optional=true");
    propertyLocations.add("file:${user.home}/application.properties;optional=true");
    propertyLocations.add("file:${camel.config.location};optional=true");
    if (System.getProperty("camel.config.locations") != null) {
      for (String location : System.getProperty("camel.config.locations").split(",")) {
        propertyLocations.add("file:" + location + ";optional=true");
      }
    }
    propertyLocations.add("file:${env:CAMEL_CONFIG_LOCATION};optional=true");
    if (System.getenv("CAMEL_CONFIG_LOCATIONS") != null) {
      for (String location : System.getenv("CAMEL_CONFIG_LOCATIONS").split(",")) {
        propertyLocations.add("file:" + location + ";optional=true");
      }
    }
    properties.setLocations(propertyLocations);
    
    Properties overrideProperties = new Properties();
    overrideProperties.putAll(System.getenv());
    overrideProperties.putAll(System.getProperties());
    properties.setOverrideProperties(overrideProperties);
  }
  
  @Override
  public void configure() throws Exception {
    
    from("direct:appraise")
      .log("Fetching appraisal for [${body}]")
      .process(addressProcessor)
      .toD("jpa:org.apache.camel.examples.Appraisal?query=select o from org.apache.camel.examples.Appraisal o where o.address.street = '${body.street}' and (o.address.unit = '${body.unit}' or o.address.unit is null) and o.address.city = '${body.city}' and o.address.state = '${body.state}' and o.address.zip = '${body.zip}' order by o.version desc&maximumResults=1")
      .filter().simple("${body} == ${null} || ${body.size()} == 0")
        .setBody().constant(null)
      .end()
      .setBody().simple("${body[0].amount}")
    ;
    
    from("file:{{ingest.directory}}?delete=true")
      .onException(UnmarshalException.class)
        .handled(true)
        .log("Error processing appraisal: [${exception.message}]")
        .setHeader("AuditOrigin", constant("AppraisalIngest"))
        .setHeader("AuditType", constant("FAILED_BATCH"))
        .setHeader("AuditMessage", simple("${exception.message}"))
        .marshal().jaxb("org.apache.camel.examples")
        .marshal().base64(Integer.MAX_VALUE, "\r\n", false)
        .to("direct:postAudit")
      .end()
      .log("Processing file: [${headers.CamelFileName}]")
      .transacted("PROPAGATION_REQUIRED")
      .unmarshal().jaxb("org.apache.camel.examples")
      .split()
        .simple("${body.appraisals}")
        .shareUnitOfWork()
        .parallelProcessing(false)
        .to("direct:workBatchItem")
      .end()
    ;
    
    from("direct:workBatchItem")
      .onException(BeanValidationException.class)
        .handled(true)
        .log("Error processing appraisal: [${exception.message}]")
        .setHeader("AuditOrigin", constant("AppraisalIngest"))
        .setHeader("AuditType", constant("FAILED_ITEM"))
        .setHeader("AuditMessage", simple("${exception.message}"))
        .marshal().jaxb("org.apache.camel.examples")
        .marshal().base64(Integer.MAX_VALUE, "\r\n", false)
        .to("direct:postAudit")
      .end()
      .log("Processing appraisal: [${body}]")
      .to("bean-validator:AppraisalValidation")
      .to("jpa:org.apache.camel.examples.Appraisal")
    ;
    
    from("direct:postAudit")
      .setHeader(Exchange.HTTP_METHOD, constant("POST"))
      .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
      .process(auditMessageProcessor)
      .marshal().json(JsonLibrary.Jackson)
      .log("Sending audit message: [${body}]")
      .to("http4://{{audit.host}}:{{audit.port}}{{audit.path}}")
    ;
  }
}
