
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeSecurityGroupsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeSecurityGroupsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="securityGroupSet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}DescribeSecurityGroupsSetType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeSecurityGroupsType", propOrder = {
    "securityGroupSet"
})
public class DescribeSecurityGroupsType {

    @XmlElement(required = true)
    protected DescribeSecurityGroupsSetType securityGroupSet;

    /**
     * Gets the value of the securityGroupSet property.
     * 
     * @return
     *     possible object is
     *     {@link DescribeSecurityGroupsSetType }
     *     
     */
    public DescribeSecurityGroupsSetType getSecurityGroupSet() {
        return securityGroupSet;
    }

    /**
     * Sets the value of the securityGroupSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescribeSecurityGroupsSetType }
     *     
     */
    public void setSecurityGroupSet(DescribeSecurityGroupsSetType value) {
        this.securityGroupSet = value;
    }

}
