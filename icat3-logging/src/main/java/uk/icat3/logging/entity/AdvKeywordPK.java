package uk.icat3.logging.entity;

import java.io.Serializable;

public class AdvKeywordPK implements Serializable {
    private Long advId;
    private Long keywordId;

    public AdvKeywordPK() {
    }

    public AdvKeywordPK(Long advId, Long keywordId) {
        this.advId = advId;
        this.keywordId = keywordId;
    }

    public boolean equals(Object other) {
        if (other instanceof AdvKeywordPK) {
            final AdvKeywordPK otherAdvKeywordPK = (AdvKeywordPK) other;
            final boolean areEqual = (otherAdvKeywordPK.advId.equals(advId) && otherAdvKeywordPK.keywordId.equals(keywordId));
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

    Long getKeywordId() {
        return keywordId;
    }

    void setKeywordId(Long keywordId) {
        this.keywordId = keywordId;
    }
}
