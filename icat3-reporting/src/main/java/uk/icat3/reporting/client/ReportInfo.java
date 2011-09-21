/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.reporting.client;

import java.util.List;

/**
 *
 * @author scb24683
 */
public class ReportInfo {

    public ReportInfo() {
    }
    int id;
    String name;
    String description;
    String source;
    String target;
    List<String> parameters;

    public ReportInfo(int id, String name, String description, String source, String target, List<String> parameters) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.source = source;
        this.target = target;
        this.parameters = parameters;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
}
