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

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang.builder.ToStringBuilder;

@Embeddable
@XmlType(propOrder = { "street",
                       "unit",
                       "city",
                       "state",
                       "zip" })
@XmlAccessorType(XmlAccessType.FIELD)
public class Address implements Serializable {
 
  @NotNull
  @Pattern(regexp = "\\d+\\s+.+")
  @XmlElement(required = true)
  private String street;

  @XmlElement
  private String unit;

  @NotNull
  @XmlElement(required = true)
  private String city;

  @NotNull
  @Pattern(regexp = "[A-Z]{2}")
  @XmlElement(required = true)
  private String state;

  @NotNull
  @Pattern(regexp = "\\d{5}(-\\d{4})?")
  @XmlElement(required = true)
  private String zip;

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getZip() {
    return zip;
  }

  public void setZip(String zip) {
    this.zip = zip;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
