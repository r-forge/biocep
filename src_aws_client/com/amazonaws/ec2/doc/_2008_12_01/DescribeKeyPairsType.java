
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeKeyPairsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeKeyPairsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="keySet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}DescribeKeyPairsInfoType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeKeyPairsType", propOrder = {
    "keySet"
})
public class DescribeKeyPairsType {

    @XmlElement(required = true)
    protected DescribeKeyPairsInfoType keySet;

    /**
     * Gets the value of the keySet property.
     * 
     * @return
     *     possible object is
     *     {@link DescribeKeyPairsInfoType }
     *     
     */
    public DescribeKeyPairsInfoType getKeySet() {
        return keySet;
    }

    /**
     * Sets the value of the keySet property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescribeKeyPairsInfoType }
     *     
     */
    public void setKeySet(DescribeKeyPairsInfoType value) {
        this.keySet = value;
    }

}
