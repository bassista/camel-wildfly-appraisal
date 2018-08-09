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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.text.StringEscapeUtils;

@ApplicationScoped
public class AuditMessageProcessor implements Processor {

  @Override
  public void process(Exchange exchange) throws Exception {
    Map<String, Object> auditMessage = new HashMap<>();

    auditMessage.put("origin", StringEscapeUtils.escapeJson(exchange.getIn().getHeader("AuditOrigin", String.class)));
    auditMessage.put("type", StringEscapeUtils.escapeJson(exchange.getIn().getHeader("AuditType", String.class)));
    auditMessage.put("message", StringEscapeUtils.escapeJson(exchange.getIn().getHeader("AuditMessage", String.class)));
    auditMessage.put("data", exchange.getIn().getBody(String.class));
    auditMessage.put("createdTime", new Date());

    exchange.getIn().setBody(auditMessage);
  }
}
