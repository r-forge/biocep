
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeImageAttributeResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeImageAttributeResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requestId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="imageId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice>
 *           &lt;element name="launchPermission" type="{http://ec2.amazonaws.com/doc/2008-12-01/}LaunchPermissionListType"/>
 *           &lt;element name="productCodes" type="{http://ec2.amazonaws.com/doc/2008-12-01/}ProductCodeListType"/>
 *           &lt;element name="kernel" type="{http://ec2.amazonaws.com/doc/2008-12-01/}NullableAttributeValueType"/>
 *           &lt;element name="ramdisk" type="{http://ec2.amazonaws.com/doc/2008-12-01/}NullableAttributeValueType"/>
 *           &lt;element name="blockDeviceMapping" type="{http://ec2.amazonaws.com/doc/2008-12-01/}BlockDeviceMappingType"/>
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
@XmlType(name = "DescribeImageAttributeResponseType", propOrder = {
    "requestId",
    "imageId",
    "launchPermission",
    "productCodes",
    "kernel",
    "ramdisk",
    "blockDeviceMapping"
})
public class DescribeImageAttributeResponseType {

    @XmlElement(required = true)
    protected String requestId;
    @XmlElement(required = true)
    protected String imageId;
    protected LaunchPermissionListType launchPermission;
    protected ProductCodeListType productCodes;
    protected NullableAttributeValueType kernel;
    protected NullableAttributeValueType ramdisk;
    protected BlockDeviceMappingType blockDeviceMapping;

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
     *     {@link LaunchPermissionListType }
     *     
     */
    public LaunchPermissionListType getLaunchPermission() {
        return launchPermission;
    }

    /**
     * Sets the value of the launchPermission property.
     * 
     * @param value
     *     allowed object is
     *     {@link LaunchPermissionListType }
     *     
     */
    public void setLaunchPermission(LaunchPermissionListType value) {
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

    /**
     * Gets the value of the kernel property.
     * 
     * @return
     *     possible object is
     *     {@link NullableAttributeValueType }
     *     
     */
    public NullableAttributeValueType getKernel() {
        return kernel;
    }

    /**
     * Sets the value of the kernel property.
     * 
     * @param value
     *     allowed object is
     *     {@link NullableAttributeValueType }
     *     
     */
    public void setKernel(NullableAttributeValueType value) {
        this.kernel = value;
    }

    /**
     * Gets the value of the ramdisk property.
     * 
     * @return
     *     possible object is
     *     {@link NullableAttributeValueType }
     *     
     */
    public NullableAttributeValueType getRamdisk() {
        return ramdisk;
    }

    /**
     * Sets the value of the ramdisk property.
     * 
     * @param value
     *     allowed object is
     *     {@link NullableAttributeValueType }
     *     
     */
    public void setRamdisk(NullableAttributeValueType value) {
        this.ramdisk = value;
    }

    /**
     * Gets the value of the blockDeviceMapping property.
     * 
     * @return
     *     possible object is
     *     {@link BlockDeviceMappingType }
     *     
     */
    public BlockDeviceMappingType getBlockDeviceMapping() {
        return blockDeviceMapping;
    }

    /**
     * Sets the value of the blockDeviceMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link BlockDeviceMappingType }
     *     
     */
    public void setBlockDeviceMapping(BlockDeviceMappingType value) {
        this.blockDeviceMapping = value;
    }

}
