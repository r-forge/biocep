
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for RunningInstancesItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RunningInstancesItemType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="instanceId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="imageId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="instanceState" type="{http://ec2.amazonaws.com/doc/2008-12-01/}InstanceStateType"/>
 *         &lt;element name="privateDnsName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dnsName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="reason" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="keyName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="amiLaunchIndex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="productCodes" type="{http://ec2.amazonaws.com/doc/2008-12-01/}ProductCodesSetType" minOccurs="0"/>
 *         &lt;element name="instanceType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="launchTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="placement" type="{http://ec2.amazonaws.com/doc/2008-12-01/}PlacementResponseType" minOccurs="0"/>
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
@XmlType(name = "RunningInstancesItemType", propOrder = {
    "instanceId",
    "imageId",
    "instanceState",
    "privateDnsName",
    "dnsName",
    "reason",
    "keyName",
    "amiLaunchIndex",
    "productCodes",
    "instanceType",
    "launchTime",
    "placement",
    "kernelId",
    "ramdiskId",
    "platform"
})
public class RunningInstancesItemType {

    @XmlElement(required = true)
    protected String instanceId;
    @XmlElement(required = true)
    protected String imageId;
    @XmlElement(required = true)
    protected InstanceStateType instanceState;
    @XmlElement(required = true)
    protected String privateDnsName;
    @XmlElement(required = true)
    protected String dnsName;
    protected String reason;
    protected String keyName;
    protected String amiLaunchIndex;
    protected ProductCodesSetType productCodes;
    @XmlElement(required = true)
    protected String instanceType;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar launchTime;
    protected PlacementResponseType placement;
    protected String kernelId;
    protected String ramdiskId;
    protected String platform;

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
     * Gets the value of the instanceState property.
     * 
     * @return
     *     possible object is
     *     {@link InstanceStateType }
     *     
     */
    public InstanceStateType getInstanceState() {
        return instanceState;
    }

    /**
     * Sets the value of the instanceState property.
     * 
     * @param value
     *     allowed object is
     *     {@link InstanceStateType }
     *     
     */
    public void setInstanceState(InstanceStateType value) {
        this.instanceState = value;
    }

    /**
     * Gets the value of the privateDnsName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrivateDnsName() {
        return privateDnsName;
    }

    /**
     * Sets the value of the privateDnsName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrivateDnsName(String value) {
        this.privateDnsName = value;
    }

    /**
     * Gets the value of the dnsName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDnsName() {
        return dnsName;
    }

    /**
     * Sets the value of the dnsName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDnsName(String value) {
        this.dnsName = value;
    }

    /**
     * Gets the value of the reason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the value of the reason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReason(String value) {
        this.reason = value;
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
     * Gets the value of the amiLaunchIndex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAmiLaunchIndex() {
        return amiLaunchIndex;
    }

    /**
     * Sets the value of the amiLaunchIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAmiLaunchIndex(String value) {
        this.amiLaunchIndex = value;
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
     * Gets the value of the launchTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLaunchTime() {
        return launchTime;
    }

    /**
     * Sets the value of the launchTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLaunchTime(XMLGregorianCalendar value) {
        this.launchTime = value;
    }

    /**
     * Gets the value of the placement property.
     * 
     * @return
     *     possible object is
     *     {@link PlacementResponseType }
     *     
     */
    public PlacementResponseType getPlacement() {
        return placement;
    }

    /**
     * Sets the value of the placement property.
     * 
     * @param value
     *     allowed object is
     *     {@link PlacementResponseType }
     *     
     */
    public void setPlacement(PlacementResponseType value) {
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
