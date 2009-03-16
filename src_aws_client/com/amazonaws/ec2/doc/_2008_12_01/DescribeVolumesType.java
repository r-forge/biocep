
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeVolumesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeVolumesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="volumeSet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}DescribeVolumesSetType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeVolumesType", propOrder = {
    "volumeSet"
})
public class DescribeVolumesType {

    @XmlElement(required = true)
    protected DescribeVolumesSetType volumeSet;

    /**
     * Gets the value of the volumeSet property.
     * 
     * @return
     *     possible object is
     *     {@link DescribeVolumesSetType }
     *     
     */
    public DescribeVolumesSetType getVolumeSet() {
        return volumeSet;
    }

    /**
     * Sets the value of the volumeSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescribeVolumesSetType }
     *     
     */
    public void setVolumeSet(DescribeVolumesSetType value) {
        this.volumeSet = value;
    }

}
