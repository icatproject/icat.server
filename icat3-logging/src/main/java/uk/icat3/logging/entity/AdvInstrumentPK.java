package uk.icat3.logging.entity;

import java.io.Serializable;

public class AdvInstrumentPK implements Serializable {
    private Long advId;
    private Long instrumentId;

    public AdvInstrumentPK() {
    }

    public AdvInstrumentPK(Long advId, Long instrumentId) {
        this.advId = advId;
        this.instrumentId = instrumentId;
    }

    public boolean equals(Object other) {
        if (other instanceof AdvInstrumentPK) {
            final AdvInstrumentPK otherAdvInstrumentPK = (AdvInstrumentPK) other;
            final boolean areEqual =
                (otherAdvInstrumentPK.advId.equals(advId) && otherAdvInstrumentPK.instrumentId.equals(instrumentId));
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

    Long getInstrumentId() {
        return instrumentId;
    }

    void setInstrumentId(Long instrumentId) {
        this.instrumentId = instrumentId;
    }
}
