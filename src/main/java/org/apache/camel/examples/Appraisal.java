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
import java.util.Date;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
@Table(name = "appraisal")
@XmlType(propOrder = { "appraiser",
                       "address",
                       "amount" })
@XmlAccessorType(XmlAccessType.FIELD)
public class Appraisal implements Serializable {
 
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @XmlTransient
  private Long id;
  
  @NotNull
  @XmlElement(required = true)
  private String appraiser;

  @Embedded
  @NotNull
  @Valid
  @XmlElement(required = true)
  private Address address;

  @XmlElement(required = true)
  @NotNull
  @DecimalMin(value = "0")
  private Double amount;
  
  @Version
  @XmlTransient
  private Date version;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAppraiser() {
    return appraiser;
  }

  public void setAppraiser(String appraiser) {
    this.appraiser = appraiser;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public Double getAmount() {
    return amount;
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }

  public Date getVersion() {
    return version;
  }

  public void setVersion(Date version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
