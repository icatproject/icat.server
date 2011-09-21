package uk.icat3.logging.entity;

import java.io.Serializable;

public class AdvInvestigatorPK implements Serializable {
    private Long advId;
    private Long investigatorId;

    public AdvInvestigatorPK() {
    }

    public AdvInvestigatorPK(Long advId, Long investigatorId) {
        this.advId = advId;
        this.investigatorId = investigatorId;
    }

    public boolean equals(Object other) {
        if (other instanceof AdvInvestigatorPK) {
            final AdvInvestigatorPK otherAdvInvestigatorPK = (AdvInvestigatorPK) other;
            final boolean areEqual =
                (otherAdvInvestigatorPK.advId.equals(advId) && otherAdvInvestigatorPK.investigatorId.equals(investigatorId));
            return areEqual;
        }
        return false;
    }

    public int hashCode() {
        return super.hashCode();
    }

    Long getAdvId() {
        return advId;
    }

    void setAdvId(Long advId) {
        this.advId = advId;
    }

    Long getInvestigatorId() {
        return investigatorId;
    }

    void setInvestigatorId(Long investigatorId) {
        this.investigatorId = investigatorId;
    }
}
