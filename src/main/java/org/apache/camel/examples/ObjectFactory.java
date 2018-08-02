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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {
 
  public Appraisals createAppraisals() {
    return new Appraisals();
  }
  
  public Appraisal createAppraisal() {
    return new Appraisal();
  }
  
  public Address createAddress() {
    return new Address();
  }
  
  @XmlElementDecl(namespace = "http://camel.apache.org/examples", name = "appraisal")
  public JAXBElement<Appraisal> createAppraisal(Appraisal value) {
    return new JAXBElement<>(new QName("http://camel.apache.org/examples", "appraisal"), Appraisal.class, value);
  }
  
  @XmlElementDecl(namespace = "http://camel.apache.org/examples", name = "address")
  public JAXBElement<Address> createAddress(Address value) {
    return new JAXBElement<>(new QName("http://camel.apache.org/examples", "address"), Address.class, value);
  }
}
