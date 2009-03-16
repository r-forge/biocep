
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeAddressesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeAddressesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="publicIpsSet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}DescribeAddressesInfoType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeAddressesType", propOrder = {
    "publicIpsSet"
})
public class DescribeAddressesType {

    @XmlElement(required = true)
    protected DescribeAddressesInfoType publicIpsSet;

    /**
     * Gets the value of the publicIpsSet property.
     * 
     * @return
     *     possible object is
     *     {@link DescribeAddressesInfoType }
     *     
     */
    public DescribeAddressesInfoType getPublicIpsSet() {
        return publicIpsSet;
    }

    /**
     * Sets the value of the publicIpsSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescribeAddressesInfoType }
     *     
     */
    public void setPublicIpsSet(DescribeAddressesInfoType value) {
        this.publicIpsSet = value;
    }

}
