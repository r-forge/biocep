
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ResetImageAttributeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResetImageAttributeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="imageId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;group ref="{http://ec2.amazonaws.com/doc/2008-12-01/}ResetImageAttributesGroup"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResetImageAttributeType", propOrder = {
    "imageId",
    "launchPermission"
})
public class ResetImageAttributeType {

    @XmlElement(required = true)
    protected String imageId;
    protected EmptyElementType launchPermission;

    /**
     * Gets the value of the imageId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * Sets the value of the imageId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImageId(String value) {
        this.imageId = value;
    }

    /**
     * Gets the value of the launchPermission property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyElementType }
     *     
     */
    public EmptyElementType getLaunchPermission() {
        return launchPermission;
    }

    /**
     * Sets the value of the launchPermission property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyElementType }
     *     
     */
    public void setLaunchPermission(EmptyElementType value) {
        this.launchPermission = value;
    }

}
