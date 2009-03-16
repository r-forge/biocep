
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ModifyImageAttributeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ModifyImageAttributeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="imageId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice>
 *           &lt;element name="launchPermission" type="{http://ec2.amazonaws.com/doc/2008-12-01/}LaunchPermissionOperationType"/>
 *           &lt;element name="productCodes" type="{http://ec2.amazonaws.com/doc/2008-12-01/}ProductCodeListType"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModifyImageAttributeType", propOrder = {
    "imageId",
    "launchPermission",
    "productCodes"
})
public class ModifyImageAttributeType {

    @XmlElement(required = true)
    protected String imageId;
    protected LaunchPermissionOperationType launchPermission;
    protected ProductCodeListType productCodes;

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
     *     {@link LaunchPermissionOperationType }
     *     
     */
    public LaunchPermissionOperationType getLaunchPermission() {
        return launchPermission;
    }

    /**
     * Sets the value of the launchPermission property.
     * 
     * @param value
     *     allowed object is
     *     {@link LaunchPermissionOperationType }
     *     
     */
    public void setLaunchPermission(LaunchPermissionOperationType value) {
        this.launchPermission = value;
    }

    /**
     * Gets the value of the productCodes property.
     * 
     * @return
     *     possible object is
     *     {@link ProductCodeListType }
     *     
     */
    public ProductCodeListType getProductCodes() {
        return productCodes;
    }

    /**
     * Sets the value of the productCodes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProductCodeListType }
     *     
     */
    public void setProductCodes(ProductCodeListType value) {
        this.productCodes = value;
    }

}
