/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.data;

import java.util.Collection;

/**
 *
 * @author gjd37
 */
public class DownloadInfo {

    private String userId;
    private Collection<String> datafileNames;

    public Collection getDatafileNames() {
        return datafileNames;
    }

    public void setDatafileNames(Collection datafileNames) {
        this.datafileNames = datafileNames;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
