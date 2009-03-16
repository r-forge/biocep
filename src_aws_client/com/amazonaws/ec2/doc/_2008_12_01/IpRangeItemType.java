
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IpRangeItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IpRangeItemType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cidrIp" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IpRangeItemType", propOrder = {
    "cidrIp"
})
public class IpRangeItemType {

    @XmlElement(required = true)
    protected String cidrIp;

    /**
     * Gets the value of the cidrIp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCidrIp() {
        return cidrIp;
    }

    /**
     * Sets the value of the cidrIp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCidrIp(String value) {
        this.cidrIp = value;
    }

}
