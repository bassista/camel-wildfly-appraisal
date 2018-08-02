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
import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

@ApplicationScoped
public class AddressProcessor implements Processor {

  @Override
  public void process(Exchange exchange) throws Exception {
    String rawAddress = exchange.getIn().getBody(String.class);
    
    List<String> parts = new ArrayList<>();
    parts.addAll(Arrays.asList(rawAddress.split("\\s*,\\s*")));
    parts.addAll(Arrays.asList((parts.remove(parts.size() - 1)).split("\\s")));
    Address address = new Address();
    address.setStreet(parts.get(0));
    address.setUnit((parts.size() == 5) ? parts.get(1) : null);
    address.setCity((parts.size() == 5) ? parts.get(2) : parts.get(1));
    address.setState((parts.size() == 5) ? parts.get(3) : parts.get(2));
    address.setZip((parts.size() == 5) ? parts.get(4) : parts.get(3));
    
    exchange.getIn().setBody(address);
  }
}
