
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RunInstancesResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RunInstancesResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requestId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="reservationId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ownerId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="groupSet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}GroupSetType"/>
 *         &lt;element name="instancesSet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}RunningInstancesSetType"/>
 *         &lt;element name="requesterId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RunInstancesResponseType", propOrder = {
    "requestId",
    "reservationId",
    "ownerId",
    "groupSet",
    "instancesSet",
    "requesterId"
})
public class RunInstancesResponseType {

    @XmlElement(required = true)
    protected String requestId;
    @XmlElement(required = true)
    protected String reservationId;
    @XmlElement(required = true)
    protected String ownerId;
    @XmlElement(required = true)
    protected GroupSetType groupSet;
    @XmlElement(required = true)
    protected RunningInstancesSetType instancesSet;
    protected String requesterId;

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
     * Gets the value of the reservationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReservationId() {
        return reservationId;
    }

    /**
     * Sets the value of the reservationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReservationId(String value) {
        this.reservationId = value;
    }

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
     * Gets the value of the groupSet property.
     * 
     * @return
     *     possible object is
     *     {@link GroupSetType }
     *     
     */
    public GroupSetType getGroupSet() {
        return groupSet;
    }

    /**
     * Sets the value of the groupSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link GroupSetType }
     *     
     */
    public void setGroupSet(GroupSetType value) {
        this.groupSet = value;
    }

    /**
     * Gets the value of the instancesSet property.
     * 
     * @return
     *     possible object is
     *     {@link RunningInstancesSetType }
     *     
     */
    public RunningInstancesSetType getInstancesSet() {
        return instancesSet;
    }

    /**
     * Sets the value of the instancesSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link RunningInstancesSetType }
     *     
     */
    public void setInstancesSet(RunningInstancesSetType value) {
        this.instancesSet = value;
    }

    /**
     * Gets the value of the requesterId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequesterId() {
        return requesterId;
    }

    /**
     * Sets the value of the requesterId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequesterId(String value) {
        this.requesterId = value;
    }

}
