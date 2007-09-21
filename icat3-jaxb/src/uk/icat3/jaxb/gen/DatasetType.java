//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-382 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.09.20 at 03:11:44 PM BST 
//


package uk.icat3.jaxb.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Dataset_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Dataset_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;maxLength value="255"/>
 *     &lt;enumeration value="EXPERIMENT_RAW"/>
 *     &lt;enumeration value="EXPERIMENT_CAL"/>
 *     &lt;enumeration value="EXPERIMENT_ENG"/>
 *     &lt;enumeration value="SOURCE_MONITORING"/>
 *     &lt;enumeration value="SAMPLE"/>
 *     &lt;enumeration value="TARGET"/>
 *     &lt;enumeration value="REDUCED"/>
 *     &lt;enumeration value="ANALYZED"/>
 *     &lt;enumeration value="SIMULATION"/>
 *     &lt;enumeration value="SPECIAL_CAL"/>
 *     &lt;enumeration value="SPECIAL_USER"/>
 *     &lt;enumeration value="LASER_SHOT"/>
 *     &lt;enumeration value="NOTES"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "Dataset_Type")
@XmlEnum
public enum DatasetType {

    EXPERIMENT_RAW,
    EXPERIMENT_CAL,
    EXPERIMENT_ENG,
    SOURCE_MONITORING,
    SAMPLE,
    TARGET,
    REDUCED,
    ANALYZED,
    SIMULATION,
    SPECIAL_CAL,
    SPECIAL_USER,
    LASER_SHOT,
    NOTES;

    public String value() {
        return name();
    }

    public static DatasetType fromValue(String v) {
        return valueOf(v);
    }

}
