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
import javax.persistence.TableGenerator;

@Entity
@NamedQueries({
  @NamedQuery(name = "Download.findAll", query = "select o from Download o")
})
public class Download implements Serializable {
        @TableGenerator(name = "DOWNLOAD_GENERATOR",
                  table = "ID_SEQUENCE_TABLE", pkColumnName = "NAME",
                  valueColumnName = "VALUE", pkColumnValue = "DOWNLOAD_KEY",
                  allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,
                  generator = "DOWNLOAD_GENERATOR")
    @Column(name="DOWNLOAD_ID", nullable = false)
    private Long downloadId;
    @Column(name="DOWNLOAD_TIME")
    private Timestamp downloadTime;
    private String method;
    @Column(name="NO_FILES")
    private Long noFiles;
    @Column(name="TOTAL_SIZE")
    private Long totalSize;
    @Column(name="USER_ID")
    private String userId;
    @ManyToOne
    @JoinColumn(name = "SESSION_ID")
    private Login login;
    @OneToMany(mappedBy = "download")
    private List<DatafileDownload> datafileDownloadList;

    public Download() {
    }

    public Download(Long downloadId, Timestamp downloadTime, String method,
                    Long noFiles, Login login, Long totalSize,
                    String userId) {
        this.downloadId = downloadId;
        this.downloadTime = downloadTime;
        this.method = method;
        this.noFiles = noFiles;
        this.login = login;
        this.totalSize = totalSize;
        this.userId = userId;
    }

    public Long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(Long downloadId) {
        this.downloadId = downloadId;
    }

    public Timestamp getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(Timestamp downloadTime) {
        this.downloadTime = downloadTime;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Long getNoFiles() {
        return noFiles;
    }

    public void setNoFiles(Long noFiles) {
        this.noFiles = noFiles;
    }


    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Login getLogin() {
        return login;
    }

    public void setLogin(Login login) {
        this.login = login;
    }

    public List<DatafileDownload> getDatafileDownloadList() {
        return datafileDownloadList;
    }

    public void setDatafileDownloadList(List<DatafileDownload> datafileDownloadList) {
        this.datafileDownloadList = datafileDownloadList;
    }

    public DatafileDownload addDatafileDownload(DatafileDownload datafileDownload) {
        getDatafileDownloadList().add(datafileDownload);
        datafileDownload.setDownload(this);
        return datafileDownload;
    }

    public DatafileDownload removeDatafileDownload(DatafileDownload datafileDownload) {
        getDatafileDownloadList().remove(datafileDownload);
        datafileDownload.setDownload(null);
        return datafileDownload;
    }
}
