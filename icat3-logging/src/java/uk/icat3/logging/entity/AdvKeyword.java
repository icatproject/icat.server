package uk.icat3.logging.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@NamedQueries({
  @NamedQuery(name = "AdvKeyword.findAll", query = "select o from AdvKeyword o")
})
@Table(name = "ADV_KEYWORD")
@IdClass(AdvKeywordPK.class)
public class AdvKeyword implements Serializable {
    @Id
    @Column(name="ADV_ID", nullable = false, insertable = false, updatable = false)
    private Long advId;
    @Id
    @Column(name="KEYWORD_ID", nullable = false, insertable = false,
            updatable = false)
    private Long keywordId;
    @ManyToOne
    @JoinColumn(name = "KEYWORD_ID")
    private LogKeyword logKeyword;
    @ManyToOne
    @JoinColumn(name = "ADV_ID")
    private AdvancedSearch advancedSearch;

    public AdvKeyword() {
    }

    public AdvKeyword(AdvancedSearch advancedSearch, LogKeyword logKeyword) {
        this.advancedSearch = advancedSearch;
        this.logKeyword = logKeyword;
    }

    public Long getAdvId() {
        return advId;
    }

    public void setAdvId(Long advId) {
        this.advId = advId;
    }

    public Long getKeywordId() {
        return keywordId;
    }

    public void setKeywordId(Long keywordId) {
        this.keywordId = keywordId;
    }

    public LogKeyword getLogKeyword() {
        return logKeyword;
    }

    public void setLogKeyword(LogKeyword logKeyword) {
        this.logKeyword = logKeyword;
        if (logKeyword != null) {
            this.keywordId = logKeyword.getId();
        }
    }

    public AdvancedSearch getAdvancedSearch() {
        return advancedSearch;
    }

    public void setAdvancedSearch(AdvancedSearch advancedSearch) {
        this.advancedSearch = advancedSearch;
        if (advancedSearch != null) {
            this.advId = advancedSearch.getAdvId();
        }
    }
}
