/*
 * ProxyServers.java
 *
 * Created on 19 June 2006, 15:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.userdefault.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author gjd37
 */
@Entity
@Table(name = "MYPROXY_SERVERS")
@NamedQueries( {
    @NamedQuery(name = "ProxyServers.findById", query = "SELECT p FROM ProxyServers p WHERE p.id = :id"),
    @NamedQuery(name = "ProxyServers.findByProxyServerAddress", query = "SELECT p FROM ProxyServers p WHERE p.proxyServerAddress = :proxyServerAddress"),
    @NamedQuery(name = "ProxyServers.findByCaRootCertificate", query = "SELECT p FROM ProxyServers p WHERE p.caRootCertificate = :caRootCertificate"),
    @NamedQuery(name = "ProxyServers.findByModTime", query = "SELECT p FROM ProxyServers p WHERE p.modTime = :modTime"),
    @NamedQuery(name = "ProxyServers.findByActive", query = "SELECT p FROM ProxyServers p WHERE p.active = :active"),
    @NamedQuery(name = "ProxyServers.findAll", query = "SELECT p FROM ProxyServers p")}
)
public class ProxyServers implements Serializable {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "PROXY_SERVER_ADDRESS", nullable = false)
    private String proxyServerAddress;
    
    @Column(name = "CA_ROOT_CERTIFICATE", nullable = false, length=500)
    private String caRootCertificate;
    
    @Column(name = "PORT_NUMBER", nullable = false)
    private Integer portNumber;
    
    @Column(name = "ACTIVE", nullable = false)
    private boolean active;
    
    @Column(name = "MOD_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modTime;
    
    /**
     * Automatically updates deleted and modTime when entity is persisted
     */
    @PrePersist
    @PreUpdate
    public void prePersist(){
        modTime = new Date();
    }
    
    /** Creates a new instance of ProxyServers */
    public ProxyServers() {
    }
    
    public ProxyServers(Long id) {
        this.setId(id);
    }
    
    public ProxyServers(Long id, String proxyServerAddress, String caRootCertificate) {
        this.setId(id);
        this.setProxyServerAddress(proxyServerAddress);
        this.setCaRootCertificate(caRootCertificate);
    }
    
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getProxyServerAddress() {
        return this.proxyServerAddress;
    }
    
    public void setProxyServerAddress(String proxyServerAddress) {
        this.proxyServerAddress = proxyServerAddress;
    }
    
    public String getCaRootCertificate() {
        return this.caRootCertificate;
    }
    
    public void setCaRootCertificate(String caRootCertificate) {
        this.caRootCertificate = caRootCertificate;
    }
    
    public Date getModTime() {
        return this.modTime;
    }
    
    public int hashCode() {
        int hash = 0;
        hash += (this.getId() != null ? this.getId().hashCode() : 0);
        return hash;
    }
    
    public boolean equals(Object object) {
        if (object == null || !this.getClass().equals(object.getClass())) {
            return false;
        }
        ProxyServers other = (ProxyServers)object;
        if (this.getId() != other.getId() && (this.getId() == null || !this.getId().equals(other.getId()))) return false;
        return true;
    }
    
    public String toString() {
        //TODO change toString() implementation to return a better display name
        return  "uk.icat3.userdefault.entity.ProxyServers[id=" + id + "]";
    }
    
    public Integer getPortNumber() {
        return this.portNumber;
    }
    
    public void setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
}
