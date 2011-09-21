package uk.icat3.logging.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@NamedQueries({
  @NamedQuery(name = "ParamSearch.findAll", query = "select o from ParamSearch o")
})
@Table(name = "PARAM_SEARCH")
public class ParamSearch implements Serializable {
    private Long count;
    @Column(name="DATAFILE_INC")
    private String datafileInc;
    @Column(name="DATASET_INC")
    private String datasetInc;
    @Column(name="INVESTIGATION_INC")
    private String investigationInc;
    private String method;
    private Long offset;
    @Id
    @Column(name="PARAM_SEARCH_ID", nullable = false)
    private Long paramSearchId;
    @Column(name="SAMPLE_INC")
    private String sampleInc;
    @ManyToOne
    @JoinColumn(name = "SEARCH_ID")
    private Search search;
    @ManyToOne
    @JoinColumn(name = "WHERE_ID")
    private SimpleWhere simpleWhere;

    public ParamSearch() {
    }

    public ParamSearch(Long count, String datafileInc, String datasetInc,
                       String investigationInc, String method, Long offset,
                       Long paramSearchId, String sampleInc, Search search,
                       SimpleWhere simpleWhere) {
        this.count = count;
        this.datafileInc = datafileInc;
        this.datasetInc = datasetInc;
        this.investigationInc = investigationInc;
        this.method = method;
        this.offset = offset;
        this.paramSearchId = paramSearchId;
        this.sampleInc = sampleInc;
        this.search = search;
        this.simpleWhere = simpleWhere;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getDatafileInc() {
        return datafileInc;
    }

    public void setDatafileInc(String datafileInc) {
        this.datafileInc = datafileInc;
    }

    public String getDatasetInc() {
        return datasetInc;
    }

    public void setDatasetInc(String datasetInc) {
        this.datasetInc = datasetInc;
    }

    public String getInvestigationInc() {
        return investigationInc;
    }

    public void setInvestigationInc(String investigationInc) {
        this.investigationInc = investigationInc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public Long getParamSearchId() {
        return paramSearchId;
    }

    public void setParamSearchId(Long paramSearchId) {
        this.paramSearchId = paramSearchId;
    }

    public String getSampleInc() {
        return sampleInc;
    }

    public void setSampleInc(String sampleInc) {
        this.sampleInc = sampleInc;
    }


    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public SimpleWhere getSimpleWhere() {
        return simpleWhere;
    }

    public void setSimpleWhere(SimpleWhere simpleWhere) {
        this.simpleWhere = simpleWhere;
    }
}
