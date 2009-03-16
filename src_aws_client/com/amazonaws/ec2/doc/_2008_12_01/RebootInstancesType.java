
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RebootInstancesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RebootInstancesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="instancesSet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}RebootInstancesInfoType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RebootInstancesType", propOrder = {
    "instancesSet"
})
public class RebootInstancesType {

    @XmlElement(required = true)
    protected RebootInstancesInfoType instancesSet;

    /**
     * Gets the value of the instancesSet property.
     * 
     * @return
     *     possible object is
     *     {@link RebootInstancesInfoType }
     *     
     */
    public RebootInstancesInfoType getInstancesSet() {
        return instancesSet;
    }

    /**
     * Sets the value of the instancesSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link RebootInstancesInfoType }
     *     
     */
    public void setInstancesSet(RebootInstancesInfoType value) {
        this.instancesSet = value;
    }

}
