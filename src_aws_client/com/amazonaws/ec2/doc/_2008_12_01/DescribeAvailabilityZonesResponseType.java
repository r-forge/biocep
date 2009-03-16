
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeAvailabilityZonesResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeAvailabilityZonesResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requestId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="availabilityZoneInfo" type="{http://ec2.amazonaws.com/doc/2008-12-01/}AvailabilityZoneSetType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeAvailabilityZonesResponseType", propOrder = {
    "requestId",
    "availabilityZoneInfo"
})
public class DescribeAvailabilityZonesResponseType {

    @XmlElement(required = true)
    protected String requestId;
    @XmlElement(required = true)
    protected AvailabilityZoneSetType availabilityZoneInfo;

    /**
     * Gets the value of the requestId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the value of the requestId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestId(String value) {
        this.requestId = value;
    }

    /**
     * Gets the value of the availabilityZoneInfo property.
     * 
     * @return
     *     possible object is
     *     {@link AvailabilityZoneSetType }
     *     
     */
    public AvailabilityZoneSetType getAvailabilityZoneInfo() {
        return availabilityZoneInfo;
    }

    /**
     * Sets the value of the availabilityZoneInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link AvailabilityZoneSetType }
     *     
     */
    public void setAvailabilityZoneInfo(AvailabilityZoneSetType value) {
        this.availabilityZoneInfo = value;
    }

}
