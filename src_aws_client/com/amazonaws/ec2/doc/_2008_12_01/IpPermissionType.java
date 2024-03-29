
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IpPermissionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IpPermissionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ipProtocol" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="fromPort" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="toPort" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="groups" type="{http://ec2.amazonaws.com/doc/2008-12-01/}UserIdGroupPairSetType"/>
 *         &lt;element name="ipRanges" type="{http://ec2.amazonaws.com/doc/2008-12-01/}IpRangeSetType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IpPermissionType", propOrder = {
    "ipProtocol",
    "fromPort",
    "toPort",
    "groups",
    "ipRanges"
})
public class IpPermissionType {

    @XmlElement(required = true)
    protected String ipProtocol;
    protected int fromPort;
    protected int toPort;
    @XmlElement(required = true)
    protected UserIdGroupPairSetType groups;
    @XmlElement(required = true)
    protected IpRangeSetType ipRanges;

    /**
     * Gets the value of the ipProtocol property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIpProtocol() {
        return ipProtocol;
    }

    /**
     * Sets the value of the ipProtocol property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIpProtocol(String value) {
        this.ipProtocol = value;
    }

    /**
     * Gets the value of the fromPort property.
     * 
     */
    public int getFromPort() {
        return fromPort;
    }

    /**
     * Sets the value of the fromPort property.
     * 
     */
    public void setFromPort(int value) {
        this.fromPort = value;
    }

    /**
     * Gets the value of the toPort property.
     * 
     */
    public int getToPort() {
        return toPort;
    }

    /**
     * Sets the value of the toPort property.
     * 
     */
    public void setToPort(int value) {
        this.toPort = value;
    }

    /**
     * Gets the value of the groups property.
     * 
     * @return
     *     possible object is
     *     {@link UserIdGroupPairSetType }
     *     
     */
    public UserIdGroupPairSetType getGroups() {
        return groups;
    }

    /**
     * Sets the value of the groups property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserIdGroupPairSetType }
     *     
     */
    public void setGroups(UserIdGroupPairSetType value) {
        this.groups = value;
    }

    /**
     * Gets the value of the ipRanges property.
     * 
     * @return
     *     possible object is
     *     {@link IpRangeSetType }
     *     
     */
    public IpRangeSetType getIpRanges() {
        return ipRanges;
    }

    /**
     * Sets the value of the ipRanges property.
     * 
     * @param value
     *     allowed object is
     *     {@link IpRangeSetType }
     *     
     */
    public void setIpRanges(IpRangeSetType value) {
        this.ipRanges = value;
    }

}
