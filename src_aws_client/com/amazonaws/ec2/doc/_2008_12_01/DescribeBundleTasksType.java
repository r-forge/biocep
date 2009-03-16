
package com.amazonaws.ec2.doc._2008_12_01;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeBundleTasksType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeBundleTasksType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bundlesSet" type="{http://ec2.amazonaws.com/doc/2008-12-01/}DescribeBundleTasksInfoType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeBundleTasksType", propOrder = {
    "bundlesSet"
})
public class DescribeBundleTasksType {

    @XmlElement(required = true)
    protected DescribeBundleTasksInfoType bundlesSet;

    /**
     * Gets the value of the bundlesSet property.
     * 
     * @return
     *     possible object is
     *     {@link DescribeBundleTasksInfoType }
     *     
     */
    public DescribeBundleTasksInfoType getBundlesSet() {
        return bundlesSet;
    }

    /**
     * Sets the value of the bundlesSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescribeBundleTasksInfoType }
     *     
     */
    public void setBundlesSet(DescribeBundleTasksInfoType value) {
        this.bundlesSet = value;
    }

}
