package uk.icat3.logging.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@NamedQueries({
  @NamedQuery(name = "DatasetView.findAll", query = "select o from DatasetView o")
})
@Table(name = "DATASET_VIEW")
public class DatasetView implements Serializable {
    @Column(name="DATASET_ID")
    private Long datasetId;
        @TableGenerator(name = "SETVIEW_GENERATOR",
                  table = "ID_SEQUENCE_TABLE", pkColumnName = "NAME",
                  valueColumnName = "VALUE", pkColumnValue = "SET_VIEW_KEY",
                  allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,
                  generator = "SETVIEW_GENERATOR")
    @Column(nullable = false)
    private Long id;
    private String instrument;
    @Column(name="INVESTIGATION_ID")
    private Long investigationId;
    @Column(name="PI_ID")
    private Long piId;
    @ManyToOne
    @JoinColumn(name = "INCLUDES")
    private DatasetInclude datasetInclude;
    @ManyToOne
    @JoinColumn(name = "VIEW_ID")
    private SimpleView simpleView;

    public DatasetView() {
    }

    public DatasetView(Long datasetId, Long id, DatasetInclude datasetInclude,
                       String instrument, Long investigationId, Long piId,
                       SimpleView simpleView) {
        this.datasetId = datasetId;
        this.id = id;
        this.datasetInclude = datasetInclude;
        this.instrument = instrument;
        this.investigationId = investigationId;
        this.piId = piId;
        this.simpleView = simpleView;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public Long getInvestigationId() {
        return investigationId;
    }

    public void setInvestigationId(Long investigationId) {
        this.investigationId = investigationId;
    }

    public Long getPiId() {
        return piId;
    }

    public void setPiId(Long piId) {
        this.piId = piId;
    }


    public DatasetInclude getDatasetInclude() {
        return datasetInclude;
    }

    public void setDatasetInclude(DatasetInclude datasetInclude) {
        this.datasetInclude = datasetInclude;
    }

    public SimpleView getSimpleView() {
        return simpleView;
    }

    public void setSimpleView(SimpleView simpleView) {
        this.simpleView = simpleView;
    }
}
