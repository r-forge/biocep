
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RunInstancesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RunInstancesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="imageId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="minCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="maxCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="keyName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="groupSet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}GroupSetType"/>
 *         &lt;element name="additionalInfo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userData" type="{http://ec2.amazonaws.com/doc/2008-12-01/}UserDataType" minOccurs="0"/>
 *         &lt;element name="addressingType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="instanceType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="placement" type="{http://ec2.amazonaws.com/doc/2008-12-01/}PlacementRequestType" minOccurs="0"/>
 *         &lt;element name="kernelId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ramdiskId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="blockDeviceMapping" type="{http://ec2.amazonaws.com/doc/2008-12-01/}BlockDeviceMappingType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RunInstancesType", propOrder = {
    "imageId",
    "minCount",
    "maxCount",
    "keyName",
    "groupSet",
    "additionalInfo",
    "userData",
    "addressingType",
    "instanceType",
    "placement",
    "kernelId",
    "ramdiskId",
    "blockDeviceMapping"
})
public class RunInstancesType {

    @XmlElement(required = true)
    protected String imageId;
    protected int minCount;
    protected int maxCount;
    protected String keyName;
    @XmlElement(required = true)
    protected GroupSetType groupSet;
    protected String additionalInfo;
    protected UserDataType userData;
    protected String addressingType;
    @XmlElement(required = true)
    protected String instanceType;
    protected PlacementRequestType placement;
    protected String kernelId;
    protected String ramdiskId;
    protected BlockDeviceMappingType blockDeviceMapping;

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
     * Gets the value of the minCount property.
     * 
     */
    public int getMinCount() {
        return minCount;
    }

    /**
     * Sets the value of the minCount property.
     * 
     */
    public void setMinCount(int value) {
        this.minCount = value;
    }

    /**
     * Gets the value of the maxCount property.
     * 
     */
    public int getMaxCount() {
        return maxCount;
    }

    /**
     * Sets the value of the maxCount property.
     * 
     */
    public void setMaxCount(int value) {
        this.maxCount = value;
    }

    /**
     * Gets the value of the keyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * Sets the value of the keyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyName(String value) {
        this.keyName = value;
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
     * Gets the value of the additionalInfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Sets the value of the additionalInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdditionalInfo(String value) {
        this.additionalInfo = value;
    }

    /**
     * Gets the value of the userData property.
     * 
     * @return
     *     possible object is
     *     {@link UserDataType }
     *     
     */
    public UserDataType getUserData() {
        return userData;
    }

    /**
     * Sets the value of the userData property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserDataType }
     *     
     */
    public void setUserData(UserDataType value) {
        this.userData = value;
    }

    /**
     * Gets the value of the addressingType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddressingType() {
        return addressingType;
    }

    /**
     * Sets the value of the addressingType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddressingType(String value) {
        this.addressingType = value;
    }

    /**
     * Gets the value of the instanceType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstanceType() {
        return instanceType;
    }

    /**
     * Sets the value of the instanceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstanceType(String value) {
        this.instanceType = value;
    }

    /**
     * Gets the value of the placement property.
     * 
     * @return
     *     possible object is
     *     {@link PlacementRequestType }
     *     
     */
    public PlacementRequestType getPlacement() {
        return placement;
    }

    /**
     * Sets the value of the placement property.
     * 
     * @param value
     *     allowed object is
     *     {@link PlacementRequestType }
     *     
     */
    public void setPlacement(PlacementRequestType value) {
        this.placement = value;
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
