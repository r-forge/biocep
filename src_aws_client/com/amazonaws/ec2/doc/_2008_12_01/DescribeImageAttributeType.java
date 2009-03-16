
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeImageAttributeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeImageAttributeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="imageId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;group ref="{http://ec2.amazonaws.com/doc/2008-12-01/}DescribeImageAttributesGroup"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeImageAttributeType", propOrder = {
    "imageId",
    "launchPermission",
    "productCodes",
    "kernel",
    "ramdisk",
    "blockDeviceMapping"
})
public class DescribeImageAttributeType {

    @XmlElement(required = true)
    protected String imageId;
    protected EmptyElementType launchPermission;
    protected EmptyElementType productCodes;
    protected EmptyElementType kernel;
    protected EmptyElementType ramdisk;
    protected EmptyElementType blockDeviceMapping;

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

    /**
     * Gets the value of the productCodes property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyElementType }
     *     
     */
    public EmptyElementType getProductCodes() {
        return productCodes;
    }

    /**
     * Sets the value of the productCodes property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyElementType }
     *     
     */
    public void setProductCodes(EmptyElementType value) {
        this.productCodes = value;
    }

    /**
     * Gets the value of the kernel property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyElementType }
     *     
     */
    public EmptyElementType getKernel() {
        return kernel;
    }

    /**
     * Sets the value of the kernel property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyElementType }
     *     
     */
    public void setKernel(EmptyElementType value) {
        this.kernel = value;
    }

    /**
     * Gets the value of the ramdisk property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyElementType }
     *     
     */
    public EmptyElementType getRamdisk() {
        return ramdisk;
    }

    /**
     * Sets the value of the ramdisk property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyElementType }
     *     
     */
    public void setRamdisk(EmptyElementType value) {
        this.ramdisk = value;
    }

    /**
     * Gets the value of the blockDeviceMapping property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyElementType }
     *     
     */
    public EmptyElementType getBlockDeviceMapping() {
        return blockDeviceMapping;
    }

    /**
     * Sets the value of the blockDeviceMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyElementType }
     *     
     */
    public void setBlockDeviceMapping(EmptyElementType value) {
        this.blockDeviceMapping = value;
    }

}
