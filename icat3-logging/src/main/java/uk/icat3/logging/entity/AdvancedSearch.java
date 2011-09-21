package uk.icat3.logging.entity;

import java.io.Serializable;

import java.sql.Timestamp;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@NamedQueries({
    @NamedQuery(name = "AdvancedSearch.findAll", query = "select o from AdvancedSearch o")
})
@Table(name = "ADVANCED_SEARCH")
public class AdvancedSearch implements Serializable {

    @Column(name = "ABSTRACT", length = 4000)
    private String abstract_;
    @TableGenerator(name = "ADVANCED_GENERATOR",
    table = "ID_SEQUENCE_TABLE", pkColumnName = "NAME",
    valueColumnName = "VALUE", pkColumnValue = "ADVANCED_KEY",
    allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,
    generator = "ADVANCED_GENERATOR")
    @Column(name = "ADV_ID", nullable = false)
    private Long advId;
    @Column(name = "CASE_SENSITIVE", length = 10)
    private String caseSensitive;
    private String datafile;
    @Column(name = "END_DATE")
    private Timestamp endDate;
    private String facility;
    @Column(name = "GRANT_ID")
    private Long grantId;
    private String investigator;
    @Column(name = "INV_TYPE")
    private String invType;
    @Column(name = "NO_RESULTS")
    private Long noResults;
    private String rb;
    @Column(name = "RUN_END")
    private Float runEnd;
    @Column(name = "RUN_START")
    private Float runStart;
    @Column(length = 500)
    private String sample;
    @Column(name = "START_DATE")
    private Timestamp startDate;
    @Column(name = "START_INDEX")
    private Long startIndex;
    private String title;
    @Column(name = "VISIT_ID")
    private String visitId;
    @Column(name = "SAMPLE_ID")
    private Long sampleId;
    @ManyToOne
    @JoinColumn(name = "INV_INCLUDE")
    private InvInclude invInclude1;
    @OneToMany(mappedBy = "advancedSearch")
    private List<AdvInstrument> advInstrumentList;
    @ManyToOne
    @JoinColumn(name = "SEARCH_ID")
    private Search search;
    @OneToMany(mappedBy = "advancedSearch")
    private List<AdvKeyword> advKeywordList;
    @OneToMany(mappedBy = "advancedSearch")
    private List<AdvInvestigator> advInvestigatorList;

    public AdvancedSearch() {
    }

    public AdvancedSearch(String abstract_, Long advId, String caseSensitive,
            String datafile, Timestamp endDate, String facility,
            Long grantId, InvInclude invInclude1, String invType,
            String investigator, Long noResults, String rb,
            Float runEnd, Float runStart, String sample,
            Search search, Timestamp startDate, Long startIndex,
            String title, String visitId, Long sampleId) {
        this.abstract_ = abstract_;
        this.advId = advId;
        this.caseSensitive = caseSensitive;
        this.datafile = datafile;
        this.endDate = endDate;
        this.facility = facility;
        this.grantId = grantId;
        this.invInclude1 = invInclude1;
        this.invType = invType;
        this.investigator = investigator;
        this.noResults = noResults;
        this.rb = rb;
        this.runEnd = runEnd;
        this.runStart = runStart;
        this.sample = sample;
        this.search = search;
        this.startDate = startDate;
        this.startIndex = startIndex;
        this.title = title;
        this.visitId = visitId;
        this.sampleId = sampleId;
    }

    public String getAbstract_() {
        return abstract_;
    }

    public void setAbstract_(String abstract_) {
        this.abstract_ = abstract_;
    }

    public Long getAdvId() {
        return advId;
    }

    public void setAdvId(Long advId) {
        this.advId = advId;
    }

    public String getCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(String caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public String getDatafile() {
        return datafile;
    }

    public void setDatafile(String datafile) {
        this.datafile = datafile;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public Long getGrantId() {
        return grantId;
    }

    public void setGrantId(Long grantId) {
        this.grantId = grantId;
    }

    public String getInvestigator() {
        return investigator;
    }

    public void setInvestigator(String investigator) {
        this.investigator = investigator;
    }

    public String getInvType() {
        return invType;
    }

    public void setInvType(String invType) {
        this.invType = invType;
    }

    public Long getNoResults() {
        return noResults;
    }

    public void setNoResults(Long noResults) {
        this.noResults = noResults;
    }

    public String getRb() {
        return rb;
    }

    public void setRb(String rb) {
        this.rb = rb;
    }

    public Float getRunEnd() {
        return runEnd;
    }

    public void setRunEnd(Float runEnd) {
        this.runEnd = runEnd;
    }

    public Float getRunStart() {
        return runStart;
    }

    public void setRunStart(Float runStart) {
        this.runStart = runStart;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Long startIndex) {
        this.startIndex = startIndex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public InvInclude getInvInclude1() {
        return invInclude1;
    }

    public void setInvInclude1(InvInclude invInclude1) {
        this.invInclude1 = invInclude1;
    }

    public List<AdvInstrument> getAdvInstrumentList() {
        return advInstrumentList;
    }

    public void setAdvInstrumentList(List<AdvInstrument> advInstrumentList) {
        this.advInstrumentList = advInstrumentList;
    }

    public AdvInstrument addAdvInstrument(AdvInstrument advInstrument) {
        getAdvInstrumentList().add(advInstrument);
        advInstrument.setAdvancedSearch(this);
        return advInstrument;
    }

    public AdvInstrument removeAdvInstrument(AdvInstrument advInstrument) {
        getAdvInstrumentList().remove(advInstrument);
        advInstrument.setAdvancedSearch(null);
        return advInstrument;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public List<AdvKeyword> getAdvKeywordList() {
        return advKeywordList;
    }

    public void setAdvKeywordList(List<AdvKeyword> advKeywordList) {
        this.advKeywordList = advKeywordList;
    }

    public AdvKeyword addAdvKeyword(AdvKeyword advKeyword) {
        getAdvKeywordList().add(advKeyword);
        advKeyword.setAdvancedSearch(this);
        return advKeyword;
    }

    public AdvKeyword removeAdvKeyword(AdvKeyword advKeyword) {
        getAdvKeywordList().remove(advKeyword);
        advKeyword.setAdvancedSearch(null);
        return advKeyword;
    }

    public List<AdvInvestigator> getAdvInvestigatorList() {
        return advInvestigatorList;
    }

    public void setAdvInvestigatorList(List<AdvInvestigator> advInvestigatorList) {
        this.advInvestigatorList = advInvestigatorList;
    }

    public AdvInvestigator addAdvInvestigator(AdvInvestigator advInvestigator) {
        getAdvInvestigatorList().add(advInvestigator);
        advInvestigator.setAdvancedSearch(this);
        return advInvestigator;
    }

    public AdvInvestigator removeAdvInvestigator(AdvInvestigator advInvestigator) {
        getAdvInvestigatorList().remove(advInvestigator);
        advInvestigator.setAdvancedSearch(null);
        return advInvestigator;
    }

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }
}
