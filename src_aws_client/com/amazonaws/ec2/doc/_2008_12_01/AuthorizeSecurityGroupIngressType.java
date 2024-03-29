
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AuthorizeSecurityGroupIngressType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuthorizeSecurityGroupIngressType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="userId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="groupName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ipPermissions" type="{http://ec2.amazonaws.com/doc/2008-12-01/}IpPermissionSetType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuthorizeSecurityGroupIngressType", propOrder = {
    "userId",
    "groupName",
    "ipPermissions"
})
public class AuthorizeSecurityGroupIngressType {

    @XmlElement(required = true)
    protected String userId;
    @XmlElement(required = true)
    protected String groupName;
    @XmlElement(required = true)
    protected IpPermissionSetType ipPermissions;

    /**
     * Gets the value of the userId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the value of the userId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserId(String value) {
        this.userId = value;
    }

    /**
     * Gets the value of the groupName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the value of the groupName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupName(String value) {
        this.groupName = value;
    }

    /**
     * Gets the value of the ipPermissions property.
     * 
     * @return
     *     possible object is
     *     {@link IpPermissionSetType }
     *     
     */
    public IpPermissionSetType getIpPermissions() {
        return ipPermissions;
    }

    /**
     * Sets the value of the ipPermissions property.
     * 
     * @param value
     *     allowed object is
     *     {@link IpPermissionSetType }
     *     
     */
    public void setIpPermissions(IpPermissionSetType value) {
        this.ipPermissions = value;
    }

}
