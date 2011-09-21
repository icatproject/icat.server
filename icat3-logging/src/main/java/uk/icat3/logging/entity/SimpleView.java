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
  @NamedQuery(name = "SimpleView.findAll", query = "select o from SimpleView o")
})
@Table(name = "SIMPLE_VIEW")
public class SimpleView implements Serializable {
    private String method;
    @Column(name="USER_ID")
    private String userId;
        @TableGenerator(name = "VIEW_GENERATOR",
                  table = "ID_SEQUENCE_TABLE", pkColumnName = "NAME",
                  valueColumnName = "VALUE", pkColumnValue = "VIEW_KEY",
                  allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,
                  generator = "VIEW_GENERATOR")
    @Column(name="VIEW_ID", nullable = false)
    private Long viewId;
    @Column(name="VIEW_TIME")
    private Timestamp viewTime;
    @OneToMany(mappedBy = "simpleView")
    private List<DatafileView> datafileViewList;
    @OneToMany(mappedBy = "simpleView")
    private List<DatasetView> datasetViewList;
    @ManyToOne
    @JoinColumn(name = "SESSION_ID")
    private Login login;
    @OneToMany(mappedBy = "simpleView")
    private List<InvestigationView> investigationViewList;

    public SimpleView() {
    }

    public SimpleView(String method, Login login, String userId,
                      Long viewId, Timestamp viewTime) {
        this.method = method;
        this.login = login;
        this.userId = userId;
        this.viewId = viewId;
        this.viewTime = viewTime;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getViewId() {
        return viewId;
    }

    public void setViewId(Long viewId) {
        this.viewId = viewId;
    }

    public Timestamp getViewTime() {
        return viewTime;
    }

    public void setViewTime(Timestamp viewTime) {
        this.viewTime = viewTime;
    }

    public List<DatafileView> getDatafileViewList() {
        return datafileViewList;
    }

    public void setDatafileViewList(List<DatafileView> datafileViewList) {
        this.datafileViewList = datafileViewList;
    }

    public DatafileView addDatafileView(DatafileView datafileView) {
        getDatafileViewList().add(datafileView);
        datafileView.setSimpleView(this);
        return datafileView;
    }

    public DatafileView removeDatafileView(DatafileView datafileView) {
        getDatafileViewList().remove(datafileView);
        datafileView.setSimpleView(null);
        return datafileView;
    }

    public List<DatasetView> getDatasetViewList() {
        return datasetViewList;
    }

    public void setDatasetViewList(List<DatasetView> datasetViewList) {
        this.datasetViewList = datasetViewList;
    }

    public DatasetView addDatasetView(DatasetView datasetView) {
        getDatasetViewList().add(datasetView);
        datasetView.setSimpleView(this);
        return datasetView;
    }

    public DatasetView removeDatasetView(DatasetView datasetView) {
        getDatasetViewList().remove(datasetView);
        datasetView.setSimpleView(null);
        return datasetView;
    }

    public Login getLogin() {
        return login;
    }

    public void setLogin(Login login) {
        this.login = login;
    }

    public List<InvestigationView> getInvestigationViewList() {
        return investigationViewList;
    }

    public void setInvestigationViewList(List<InvestigationView> investigationViewList) {
        this.investigationViewList = investigationViewList;
    }

    public InvestigationView addInvestigationView(InvestigationView investigationView) {
        getInvestigationViewList().add(investigationView);
        investigationView.setSimpleView(this);
        return investigationView;
    }

    public InvestigationView removeInvestigationView(InvestigationView investigationView) {
        getInvestigationViewList().remove(investigationView);
        investigationView.setSimpleView(null);
        return investigationView;
    }
}
