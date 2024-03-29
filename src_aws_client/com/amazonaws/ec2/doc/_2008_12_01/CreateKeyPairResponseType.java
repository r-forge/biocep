
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CreateKeyPairResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CreateKeyPairResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requestId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="keyName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="keyFingerprint" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="keyMaterial" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateKeyPairResponseType", propOrder = {
    "requestId",
    "keyName",
    "keyFingerprint",
    "keyMaterial"
})
public class CreateKeyPairResponseType {

    @XmlElement(required = true)
    protected String requestId;
    @XmlElement(required = true)
    protected String keyName;
    @XmlElement(required = true)
    protected String keyFingerprint;
    @XmlElement(required = true)
    protected String keyMaterial;

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
     * Gets the value of the keyFingerprint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyFingerprint() {
        return keyFingerprint;
    }

    /**
     * Sets the value of the keyFingerprint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyFingerprint(String value) {
        this.keyFingerprint = value;
    }

    /**
     * Gets the value of the keyMaterial property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyMaterial() {
        return keyMaterial;
    }

    /**
     * Sets the value of the keyMaterial property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyMaterial(String value) {
        this.keyMaterial = value;
    }

}
