
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeSnapshotsResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeSnapshotsResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requestId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="snapshotSet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}DescribeSnapshotsSetResponseType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeSnapshotsResponseType", propOrder = {
    "requestId",
    "snapshotSet"
})
public class DescribeSnapshotsResponseType {

    @XmlElement(required = true)
    protected String requestId;
    @XmlElement(required = true)
    protected DescribeSnapshotsSetResponseType snapshotSet;

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
     * Gets the value of the snapshotSet property.
     * 
     * @return
     *     possible object is
     *     {@link DescribeSnapshotsSetResponseType }
     *     
     */
    public DescribeSnapshotsSetResponseType getSnapshotSet() {
        return snapshotSet;
    }

    /**
     * Sets the value of the snapshotSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescribeSnapshotsSetResponseType }
     *     
     */
    public void setSnapshotSet(DescribeSnapshotsSetResponseType value) {
        this.snapshotSet = value;
    }

}
