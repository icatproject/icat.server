package org.icatproject.core.entity;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

@XmlRootElement
public class FieldSet implements Serializable {
    @XmlElement(name = "field")
    private Object[] items;

    public FieldSet() {
        items = new Object[0];
    }

    public FieldSet(Object[] items) {
        this.items = items;
    }

    public Object[] getItems() {
        return items;
    }

    public void setItems() {
        this.items = items;
    }
}