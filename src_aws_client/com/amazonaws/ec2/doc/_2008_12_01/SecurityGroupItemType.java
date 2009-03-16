
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SecurityGroupItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SecurityGroupItemType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ownerId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="groupName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="groupDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
@XmlType(name = "SecurityGroupItemType", propOrder = {
    "ownerId",
    "groupName",
    "groupDescription",
    "ipPermissions"
})
public class SecurityGroupItemType {

    @XmlElement(required = true)
    protected String ownerId;
    @XmlElement(required = true)
    protected String groupName;
    @XmlElement(required = true)
    protected String groupDescription;
    @XmlElement(required = true)
    protected IpPermissionSetType ipPermissions;

    /**
     * Gets the value of the ownerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * Sets the value of the ownerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerId(String value) {
        this.ownerId = value;
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
     * Gets the value of the groupDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupDescription() {
        return groupDescription;
    }

    /**
     * Sets the value of the groupDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupDescription(String value) {
        this.groupDescription = value;
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
