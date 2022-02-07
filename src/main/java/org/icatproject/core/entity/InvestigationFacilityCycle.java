package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("Many to many relationship between investigation and facilityCycle. "
        + "Allows investigations to belong to multiple cycles at once. "
        + "This removes the need to calculate which cycle an investigation "
        + "belongs to based on dates.")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "FACILITYCYCLE_ID", "INVESTIGATION_ID" }) })
public class InvestigationFacilityCycle extends EntityBaseBean implements Serializable {


    @JoinColumn(name = "FACILITYCYCLE_ID", nullable = false)
    @ManyToOne
    private FacilityCycle facilityCycle;

    @JoinColumn(name = "INVESTIGATION_ID", nullable = false)
    @ManyToOne
    private Investigation investigation;

    /* Needed for JPA */
    public InvestigationFacilityCycle() {
    }

    public FacilityCycle getFacilityCycle() {
        return facilityCycle;
    }

    public void setFacilityCycle(FacilityCycle facilityCycle) {
        this.facilityCycle = facilityCycle;
    }

    public Investigation getInvestigation() {
        return investigation;
    }

    public void setInvestigation(Investigation investigation) {
        this.investigation = investigation;
    }

}
