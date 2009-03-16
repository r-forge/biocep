
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeAvailabilityZonesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeAvailabilityZonesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="availabilityZoneSet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}DescribeAvailabilityZonesSetType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeAvailabilityZonesType", propOrder = {
    "availabilityZoneSet"
})
public class DescribeAvailabilityZonesType {

    @XmlElement(required = true)
    protected DescribeAvailabilityZonesSetType availabilityZoneSet;

    /**
     * Gets the value of the availabilityZoneSet property.
     * 
     * @return
     *     possible object is
     *     {@link DescribeAvailabilityZonesSetType }
     *     
     */
    public DescribeAvailabilityZonesSetType getAvailabilityZoneSet() {
        return availabilityZoneSet;
    }

    /**
     * Sets the value of the availabilityZoneSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescribeAvailabilityZonesSetType }
     *     
     */
    public void setAvailabilityZoneSet(DescribeAvailabilityZonesSetType value) {
        this.availabilityZoneSet = value;
    }

}
