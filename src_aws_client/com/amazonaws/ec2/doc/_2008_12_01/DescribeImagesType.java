
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeImagesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeImagesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="executableBySet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}DescribeImagesExecutableBySetType" minOccurs="0"/>
 *         &lt;element name="imagesSet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}DescribeImagesInfoType"/>
 *         &lt;element name="ownersSet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}DescribeImagesOwnersType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeImagesType", propOrder = {
    "executableBySet",
    "imagesSet",
    "ownersSet"
})
public class DescribeImagesType {

    protected DescribeImagesExecutableBySetType executableBySet;
    @XmlElement(required = true)
    protected DescribeImagesInfoType imagesSet;
    protected DescribeImagesOwnersType ownersSet;

    /**
     * Gets the value of the executableBySet property.
     * 
     * @return
     *     possible object is
     *     {@link DescribeImagesExecutableBySetType }
     *     
     */
    public DescribeImagesExecutableBySetType getExecutableBySet() {
        return executableBySet;
    }

    /**
     * Sets the value of the executableBySet property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescribeImagesExecutableBySetType }
     *     
     */
    public void setExecutableBySet(DescribeImagesExecutableBySetType value) {
        this.executableBySet = value;
    }

    /**
     * Gets the value of the imagesSet property.
     * 
     * @return
     *     possible object is
     *     {@link DescribeImagesInfoType }
     *     
     */
    public DescribeImagesInfoType getImagesSet() {
        return imagesSet;
    }

    /**
     * Sets the value of the imagesSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescribeImagesInfoType }
     *     
     */
    public void setImagesSet(DescribeImagesInfoType value) {
        this.imagesSet = value;
    }

    /**
     * Gets the value of the ownersSet property.
     * 
     * @return
     *     possible object is
     *     {@link DescribeImagesOwnersType }
     *     
     */
    public DescribeImagesOwnersType getOwnersSet() {
        return ownersSet;
    }

    /**
     * Sets the value of the ownersSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescribeImagesOwnersType }
     *     
     */
    public void setOwnersSet(DescribeImagesOwnersType value) {
        this.ownersSet = value;
    }

}
