/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.logging.entity;

/**
 *
 * @author scb24683
 */
public class LoggedDownload {

    Long investigationId;
    Long datafileId;
    Long datasetId;
    String filename;
    String filesize;
    String userId;

    public Long getDatafileId() {
        return datafileId;
    }

    public void setDatafileId(Long datafileId) {
        this.datafileId = datafileId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilesize() {
        return filesize;
    }

    public void setFilesize(String filesize) {
        this.filesize = filesize;
    }

    public Long getInvestigationId() {
        return investigationId;
    }

    public void setInvestigationId(Long investigationId) {
        this.investigationId = investigationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }
}
