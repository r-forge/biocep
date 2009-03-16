
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LaunchPermissionOperationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LaunchPermissionOperationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="add" type="{http://ec2.amazonaws.com/doc/2008-12-01/}LaunchPermissionListType"/>
 *         &lt;element name="remove" type="{http://ec2.amazonaws.com/doc/2008-12-01/}LaunchPermissionListType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LaunchPermissionOperationType", propOrder = {
    "add",
    "remove"
})
public class LaunchPermissionOperationType {

    protected LaunchPermissionListType add;
    protected LaunchPermissionListType remove;

    /**
     * Gets the value of the add property.
     * 
     * @return
     *     possible object is
     *     {@link LaunchPermissionListType }
     *     
     */
    public LaunchPermissionListType getAdd() {
        return add;
    }

    /**
     * Sets the value of the add property.
     * 
     * @param value
     *     allowed object is
     *     {@link LaunchPermissionListType }
     *     
     */
    public void setAdd(LaunchPermissionListType value) {
        this.add = value;
    }

    /**
     * Gets the value of the remove property.
     * 
     * @return
     *     possible object is
     *     {@link LaunchPermissionListType }
     *     
     */
    public LaunchPermissionListType getRemove() {
        return remove;
    }

    /**
     * Sets the value of the remove property.
     * 
     * @param value
     *     allowed object is
     *     {@link LaunchPermissionListType }
     *     
     */
    public void setRemove(LaunchPermissionListType value) {
        this.remove = value;
    }

}
