
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TerminateInstancesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TerminateInstancesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="instancesSet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}TerminateInstancesInfoType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TerminateInstancesType", propOrder = {
    "instancesSet"
})
public class TerminateInstancesType {

    @XmlElement(required = true)
    protected TerminateInstancesInfoType instancesSet;

    /**
     * Gets the value of the instancesSet property.
     * 
     * @return
     *     possible object is
     *     {@link TerminateInstancesInfoType }
     *     
     */
    public TerminateInstancesInfoType getInstancesSet() {
        return instancesSet;
    }

    /**
     * Sets the value of the instancesSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link TerminateInstancesInfoType }
     *     
     */
    public void setInstancesSet(TerminateInstancesInfoType value) {
        this.instancesSet = value;
    }

}
