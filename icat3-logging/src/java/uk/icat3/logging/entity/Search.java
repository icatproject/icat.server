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
  @NamedQuery(name = "Search.findAll", query = "select o from Search o")
})
public class Search implements Serializable {
    private String method;
        @TableGenerator(name = "SEARCH_GENERATOR",
                  table = "ID_SEQUENCE_TABLE", pkColumnName = "NAME",
                  valueColumnName = "VALUE", pkColumnValue = "SEARCH_KEY",
                  allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,
                  generator = "SEARCH_GENERATOR")
    @Column(name="SEARCH_ID", nullable = false)
    private Long searchId;
    @Column(name="SEARCH_TIME")
    private Timestamp searchTime;
    @Column(name="USER_ID")
    private String userId;
    @ManyToOne
    @JoinColumn(name = "SESSION_ID")
    private Login login;
    @OneToMany(mappedBy = "search")
    private List<ParamSearch> paramSearchList;
    @OneToMany(mappedBy = "search")
    private List<AdvancedSearch> advancedSearchList;

    public Search() {
    }

    public Search(String method, Long searchId, Timestamp searchTime,
                  Login login, String userId) {
        this.method = method;
        this.searchId = searchId;
        this.searchTime = searchTime;
        this.login = login;
        this.userId = userId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Long getSearchId() {
        return searchId;
    }

    public void setSearchId(Long searchId) {
        this.searchId = searchId;
    }

    public Timestamp getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(Timestamp searchTime) {
        this.searchTime = searchTime;
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

    public List<ParamSearch> getParamSearchList() {
        return paramSearchList;
    }

    public void setParamSearchList(List<ParamSearch> paramSearchList) {
        this.paramSearchList = paramSearchList;
    }

    public ParamSearch addParamSearch(ParamSearch paramSearch) {
        getParamSearchList().add(paramSearch);
        paramSearch.setSearch(this);
        return paramSearch;
    }

    public ParamSearch removeParamSearch(ParamSearch paramSearch) {
        getParamSearchList().remove(paramSearch);
        paramSearch.setSearch(null);
        return paramSearch;
    }

    public List<AdvancedSearch> getAdvancedSearchList() {
        return advancedSearchList;
    }

    public void setAdvancedSearchList(List<AdvancedSearch> advancedSearchList) {
        this.advancedSearchList = advancedSearchList;
    }

    public AdvancedSearch addAdvancedSearch(AdvancedSearch advancedSearch) {
        getAdvancedSearchList().add(advancedSearch);
        advancedSearch.setSearch(this);
        return advancedSearch;
    }

    public AdvancedSearch removeAdvancedSearch(AdvancedSearch advancedSearch) {
        getAdvancedSearchList().remove(advancedSearch);
        advancedSearch.setSearch(null);
        return advancedSearch;
    }
}
