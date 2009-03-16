
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeRegionsResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeRegionsResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requestId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="regionInfo" type="{http://ec2.amazonaws.com/doc/2008-12-01/}RegionSetType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeRegionsResponseType", propOrder = {
    "requestId",
    "regionInfo"
})
public class DescribeRegionsResponseType {

    @XmlElement(required = true)
    protected String requestId;
    @XmlElement(required = true)
    protected RegionSetType regionInfo;

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
     * Gets the value of the regionInfo property.
     * 
     * @return
     *     possible object is
     *     {@link RegionSetType }
     *     
     */
    public RegionSetType getRegionInfo() {
        return regionInfo;
    }

    /**
     * Sets the value of the regionInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegionSetType }
     *     
     */
    public void setRegionInfo(RegionSetType value) {
        this.regionInfo = value;
    }

}
