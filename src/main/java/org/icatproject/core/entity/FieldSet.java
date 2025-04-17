package org.icatproject.core.entity;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlElement;
import java.io.Serializable;

@XmlRootElement
public class FieldSet implements Serializable {
    @XmlElement(name = "fields", nillable = true)
    private Object[] fields;

    public FieldSet() {
        fields = new Object[0];
    }

    public FieldSet(Object[] fields) {
        this.fields = fields;
    }

    public Object[] getFields() {
        return fields;
    }
}
