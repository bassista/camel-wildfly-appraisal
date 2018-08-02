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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.UserTransaction;
import javax.xml.bind.UnmarshalException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;

@ApplicationScoped
@ContextName("camel-context")
public class CamelConfig extends RouteBuilder {

  @Inject
  @ConfigurationValue("application.ingest.directory")
  private String ingestDirectory;

  @Inject
  @ConfigurationValue("application.audit.host")
  private String auditHost;
  
  @Inject
  @ConfigurationValue("application.audit.port")
  private String auditPort;
  
  @Inject
  @ConfigurationValue("application.audit.path")
  private String auditPath;
  
  @Inject
  private AddressProcessor addressProcessor;
  
  @Produces 
  @Named("transactionManager")
  public PlatformTransactionManager transactionManager(UserTransaction userTransaction) {
    JtaTransactionManager transactionManager = new JtaTransactionManager(userTransaction);
    transactionManager.afterPropertiesSet();
    return transactionManager;
  }
  
  @Produces
  @Named("requiredTransactionPolicy")
  public SpringTransactionPolicy requiredTransactionPolicy(PlatformTransactionManager transactionManager) {
    SpringTransactionPolicy policy = new SpringTransactionPolicy(transactionManager);
    policy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
    return policy;
  }
  
  @Override
  public void configure() throws Exception {
    
    from("direct:soap_appraise")
      .log("Fetching appraisal for [${body}]")
      .process(addressProcessor)
      .toD("jpa:org.apache.camel.examples.Appraisal?persistenceUnit=mysqlPU&query=select o from org.apache.camel.examples.Appraisal o where o.address.street = '${body.street}' and (o.address.unit = '${body.unit}' or o.address.unit is null) and o.address.city = '${body.city}' and o.address.state = '${body.state}' and o.address.zip = '${body.zip}' order by o.version desc&maximumResults=1")
      .filter().simple("${body} == ${null} || ${body.size()} == 0")
        .setBody().constant(null)
      .end()
      .setBody().simple("${body[0].amount}")
    ;
    
    fromF("file:%s?delete=true", ingestDirectory)
      .onException(UnmarshalException.class)
        .handled(true)
        .log("Error processing appraisal: [${exception.message}]")
        .setHeader("AuditOrigin", constant("AppraisalIngest"))
        .setHeader("AuditType", constant("FAILED_BATCH"))
        .setHeader("AuditMessage", simple("${exception.message}"))
        .marshal().jaxb("org.apache.camel.examples")
        .marshal().base64(Integer.MAX_VALUE, "\r\n", false)
        .to("direct:post_audit")
      .end()
      .log("Processing file: [${headers.CamelFileName}]")
      .transacted("requiredTransactionPolicy")
      .unmarshal().jaxb("org.apache.camel.examples")
      .split()
        .simple("${body.appraisals}")
        .shareUnitOfWork()
        .parallelProcessing(false)
        .to("direct:handle_appraisal")
      .end()
    ;
    
    from("direct:handle_appraisal")
      .onException(BeanValidationException.class)
        .handled(true)
        .log("Error processing appraisal: [${exception.message}]")
        .setHeader("AuditOrigin", constant("AppraisalIngest"))
        .setHeader("AuditType", constant("FAILED_ITEM"))
        .setHeader("AuditMessage", simple("${exception.message}"))
        .marshal().jaxb("org.apache.camel.examples")
        .marshal().base64(Integer.MAX_VALUE, "\r\n", false)
        .to("direct:post_audit")
      .end()
      .log("Processing appraisal: [${body}]")
      .to("bean-validator:AppraisalValidation")
      .to("jpa:org.apache.camel.examples.Appraisal?persistenceUnit=mysqlPU&transactionManager=#transactionManager")
    ;
        
    from("direct:post_audit")
      .setHeader(Exchange.HTTP_METHOD, constant("POST"))
      .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
      .setBody().simple("{ \"origin\": \"${header.AuditOrigin}\", \"type\": \"${header.AuditType}\", \"message\": \"${header.AuditMessage}\", \"data\": \"${body}\" }")
      .toF("http4://%s:%s/%s", auditHost, auditPort, auditPath.replaceFirst("^/", ""))
    ;
  }
}
