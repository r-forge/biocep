
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeImagesResponseItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeImagesResponseItemType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="imageId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="imageLocation" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="imageState" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="imageOwnerId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="isPublic" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="productCodes" type="{http://ec2.amazonaws.com/doc/2008-12-01/}ProductCodesSetType" minOccurs="0"/>
 *         &lt;element name="architecture" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="imageType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="kernelId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ramdiskId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="platform" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeImagesResponseItemType", propOrder = {
    "imageId",
    "imageLocation",
    "imageState",
    "imageOwnerId",
    "isPublic",
    "productCodes",
    "architecture",
    "imageType",
    "kernelId",
    "ramdiskId",
    "platform"
})
public class DescribeImagesResponseItemType {

    @XmlElement(required = true)
    protected String imageId;
    @XmlElement(required = true)
    protected String imageLocation;
    @XmlElement(required = true)
    protected String imageState;
    @XmlElement(required = true)
    protected String imageOwnerId;
    protected boolean isPublic;
    protected ProductCodesSetType productCodes;
    protected String architecture;
    protected String imageType;
    protected String kernelId;
    protected String ramdiskId;
    protected String platform;

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
     * Gets the value of the imageLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImageLocation() {
        return imageLocation;
    }

    /**
     * Sets the value of the imageLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImageLocation(String value) {
        this.imageLocation = value;
    }

    /**
     * Gets the value of the imageState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImageState() {
        return imageState;
    }

    /**
     * Sets the value of the imageState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImageState(String value) {
        this.imageState = value;
    }

    /**
     * Gets the value of the imageOwnerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImageOwnerId() {
        return imageOwnerId;
    }

    /**
     * Sets the value of the imageOwnerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImageOwnerId(String value) {
        this.imageOwnerId = value;
    }

    /**
     * Gets the value of the isPublic property.
     * 
     */
    public boolean isIsPublic() {
        return isPublic;
    }

    /**
     * Sets the value of the isPublic property.
     * 
     */
    public void setIsPublic(boolean value) {
        this.isPublic = value;
    }

    /**
     * Gets the value of the productCodes property.
     * 
     * @return
     *     possible object is
     *     {@link ProductCodesSetType }
     *     
     */
    public ProductCodesSetType getProductCodes() {
        return productCodes;
    }

    /**
     * Sets the value of the productCodes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProductCodesSetType }
     *     
     */
    public void setProductCodes(ProductCodesSetType value) {
        this.productCodes = value;
    }

    /**
     * Gets the value of the architecture property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArchitecture() {
        return architecture;
    }

    /**
     * Sets the value of the architecture property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArchitecture(String value) {
        this.architecture = value;
    }

    /**
     * Gets the value of the imageType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImageType() {
        return imageType;
    }

    /**
     * Sets the value of the imageType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImageType(String value) {
        this.imageType = value;
    }

    /**
     * Gets the value of the kernelId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKernelId() {
        return kernelId;
    }

    /**
     * Sets the value of the kernelId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKernelId(String value) {
        this.kernelId = value;
    }

    /**
     * Gets the value of the ramdiskId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRamdiskId() {
        return ramdiskId;
    }

    /**
     * Sets the value of the ramdiskId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRamdiskId(String value) {
        this.ramdiskId = value;
    }

    /**
     * Gets the value of the platform property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Sets the value of the platform property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlatform(String value) {
        this.platform = value;
    }

}
