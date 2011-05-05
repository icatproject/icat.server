package uk.icat3.logging.entity;

import java.io.Serializable;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@NamedQueries({
  @NamedQuery(name = "DatasetInclude.findAll", query = "select o from DatasetInclude o")
})
@Table(name = "DATASET_INCLUDE")
public class DatasetInclude implements Serializable {
    @Id
    @Column(nullable = false)
    private Long id;
    private String name;
    @OneToMany(mappedBy = "datasetInclude")
    private List<DatasetView> datasetViewList;

    public DatasetInclude() {
    }

    public DatasetInclude(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DatasetView> getDatasetViewList() {
        return datasetViewList;
    }

    public void setDatasetViewList(List<DatasetView> datasetViewList) {
        this.datasetViewList = datasetViewList;
    }

    public DatasetView addDatasetView(DatasetView datasetView) {
        getDatasetViewList().add(datasetView);
        datasetView.setDatasetInclude(this);
        return datasetView;
    }

    public DatasetView removeDatasetView(DatasetView datasetView) {
        getDatasetViewList().remove(datasetView);
        datasetView.setDatasetInclude(null);
        return datasetView;
    }
}
