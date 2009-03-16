
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeSnapshotsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeSnapshotsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="snapshotSet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}DescribeSnapshotsSetType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeSnapshotsType", propOrder = {
    "snapshotSet"
})
public class DescribeSnapshotsType {

    @XmlElement(required = true)
    protected DescribeSnapshotsSetType snapshotSet;

    /**
     * Gets the value of the snapshotSet property.
     * 
     * @return
     *     possible object is
     *     {@link DescribeSnapshotsSetType }
     *     
     */
    public DescribeSnapshotsSetType getSnapshotSet() {
        return snapshotSet;
    }

    /**
     * Sets the value of the snapshotSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescribeSnapshotsSetType }
     *     
     */
    public void setSnapshotSet(DescribeSnapshotsSetType value) {
        this.snapshotSet = value;
    }

}
