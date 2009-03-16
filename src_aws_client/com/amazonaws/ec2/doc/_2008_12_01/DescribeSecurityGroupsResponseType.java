
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeSecurityGroupsResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeSecurityGroupsResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requestId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="securityGroupInfo" type="{http://ec2.amazonaws.com/doc/2008-12-01/}SecurityGroupSetType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeSecurityGroupsResponseType", propOrder = {
    "requestId",
    "securityGroupInfo"
})
public class DescribeSecurityGroupsResponseType {

    @XmlElement(required = true)
    protected String requestId;
    @XmlElement(required = true)
    protected SecurityGroupSetType securityGroupInfo;

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
     * Gets the value of the securityGroupInfo property.
     * 
     * @return
     *     possible object is
     *     {@link SecurityGroupSetType }
     *     
     */
    public SecurityGroupSetType getSecurityGroupInfo() {
        return securityGroupInfo;
    }

    /**
     * Sets the value of the securityGroupInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link SecurityGroupSetType }
     *     
     */
    public void setSecurityGroupInfo(SecurityGroupSetType value) {
        this.securityGroupInfo = value;
    }

}
