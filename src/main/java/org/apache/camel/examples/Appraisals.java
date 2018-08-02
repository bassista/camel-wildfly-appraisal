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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement
@XmlType(propOrder = { "appraisals" })
@XmlAccessorType(XmlAccessType.FIELD)
public class Appraisals {
 
  @XmlElement(name = "appraisal")
  private List<Appraisal> appraisals;

  public List<Appraisal> getAppraisals() {
    if (appraisals == null) {
      appraisals = new ArrayList<>();
    }
    return appraisals;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
