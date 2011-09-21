package uk.icat3.logging.entity;

import java.io.Serializable;

import java.sql.Timestamp;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity
@NamedQueries({
  @NamedQuery(name = "Login.findAll", query = "select o from Login o")
})
public class Login implements Serializable {
    @Column(name="LOGIN_TIME")
    private Timestamp loginTime;
    @Column(name="LOGOUT_TIME")
    private Timestamp logoutTime;
    @Id
    @Column(name="SESSION_ID", nullable = false)
    private String sessionId;
    @Column(name="USER_ID")
    private String userId;
    @OneToMany(mappedBy = "login")
    private List<Download> downloadList;
    @OneToMany(mappedBy = "login")
    private List<Search> searchList;
    @OneToMany(mappedBy = "login")
    private List<SimpleView> simpleViewList;

    public Login() {
    }

    public Login(Timestamp loginTime, Timestamp logoutTime, String sessionId,
                 String userId) {
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
        this.sessionId = sessionId;
        this.userId = userId;
    }

    public Timestamp getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Timestamp loginTime) {
        this.loginTime = loginTime;
    }

    public Timestamp getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(Timestamp logoutTime) {
        this.logoutTime = logoutTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Download> getDownloadList() {
        return downloadList;
    }

    public void setDownloadList(List<Download> downloadList) {
        this.downloadList = downloadList;
    }

    public Download addDownload(Download download) {
        getDownloadList().add(download);
        download.setLogin(this);
        return download;
    }

    public Download removeDownload(Download download) {
        getDownloadList().remove(download);
        download.setLogin(null);
        return download;
    }

    public List<Search> getSearchList() {
        return searchList;
    }

    public void setSearchList(List<Search> searchList) {
        this.searchList = searchList;
    }

    public Search addSearch(Search search) {
        getSearchList().add(search);
        search.setLogin(this);
        return search;
    }

    public Search removeSearch(Search search) {
        getSearchList().remove(search);
        search.setLogin(null);
        return search;
    }

    public List<SimpleView> getSimpleViewList() {
        return simpleViewList;
    }

    public void setSimpleViewList(List<SimpleView> simpleViewList) {
        this.simpleViewList = simpleViewList;
    }

    public SimpleView addSimpleView(SimpleView simpleView) {
        getSimpleViewList().add(simpleView);
        simpleView.setLogin(this);
        return simpleView;
    }

    public SimpleView removeSimpleView(SimpleView simpleView) {
        getSimpleViewList().remove(simpleView);
        simpleView.setLogin(null);
        return simpleView;
    }
}
