
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TerminateInstancesResponseItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TerminateInstancesResponseItemType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="instanceId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="shutdownState" type="{http://ec2.amazonaws.com/doc/2008-12-01/}InstanceStateType"/>
 *         &lt;element name="previousState" type="{http://ec2.amazonaws.com/doc/2008-12-01/}InstanceStateType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TerminateInstancesResponseItemType", propOrder = {
    "instanceId",
    "shutdownState",
    "previousState"
})
public class TerminateInstancesResponseItemType {

    @XmlElement(required = true)
    protected String instanceId;
    @XmlElement(required = true)
    protected InstanceStateType shutdownState;
    @XmlElement(required = true)
    protected InstanceStateType previousState;

    /**
     * Gets the value of the instanceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Sets the value of the instanceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstanceId(String value) {
        this.instanceId = value;
    }

    /**
     * Gets the value of the shutdownState property.
     * 
     * @return
     *     possible object is
     *     {@link InstanceStateType }
     *     
     */
    public InstanceStateType getShutdownState() {
        return shutdownState;
    }

    /**
     * Sets the value of the shutdownState property.
     * 
     * @param value
     *     allowed object is
     *     {@link InstanceStateType }
     *     
     */
    public void setShutdownState(InstanceStateType value) {
        this.shutdownState = value;
    }

    /**
     * Gets the value of the previousState property.
     * 
     * @return
     *     possible object is
     *     {@link InstanceStateType }
     *     
     */
    public InstanceStateType getPreviousState() {
        return previousState;
    }

    /**
     * Sets the value of the previousState property.
     * 
     * @param value
     *     allowed object is
     *     {@link InstanceStateType }
     *     
     */
    public void setPreviousState(InstanceStateType value) {
        this.previousState = value;
    }

}
