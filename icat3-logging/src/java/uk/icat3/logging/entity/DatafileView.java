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
  @NamedQuery(name = "DatafileView.findAll", query = "select o from DatafileView o")
})
@Table(name = "DATAFILE_VIEW")
public class DatafileView implements Serializable {
    private String age;
    @Column(name="DATAFILE_ID")
    private Long datafileId;
    @Column(name="DATASET_ID")
    private Long datasetId;
    private String filename;
    private Long filesize;
    private String format;
        @TableGenerator(name = "FILEVIEW_GENERATOR",
                  table = "ID_SEQUENCE_TABLE", pkColumnName = "NAME",
                  valueColumnName = "VALUE", pkColumnValue = "FILE_VIEW_KEY",
                  allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,
                  generator = "FILEVIEW_GENERATOR")
    @Column(nullable = false)
    private Long id;
    private String instrument;
    @Column(name="INVESTIGATION_ID")
    private Long investigationId;
    @Column(name="PI_ID")
    private Long piId;
    @ManyToOne
    @JoinColumn(name = "VIEW_ID")
    private SimpleView simpleView;

    public DatafileView() {
    }

    public DatafileView(String age, Long datafileId, Long datasetId,
                        String filename, Long filesize, String format, Long id,
                        String instrument, Long investigationId, Long piId,
                        SimpleView simpleView) {
        this.age = age;
        this.datafileId = datafileId;
        this.datasetId = datasetId;
        this.filename = filename;
        this.filesize = filesize;
        this.format = format;
        this.id = id;
        this.instrument = instrument;
        this.investigationId = investigationId;
        this.piId = piId;
        this.simpleView = simpleView;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public Long getDatafileId() {
        return datafileId;
    }

    public void setDatafileId(Long datafileId) {
        this.datafileId = datafileId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getFilesize() {
        return filesize;
    }

    public void setFilesize(Long filesize) {
        this.filesize = filesize;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
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


    public SimpleView getSimpleView() {
        return simpleView;
    }

    public void setSimpleView(SimpleView simpleView) {
        this.simpleView = simpleView;
    }
}
