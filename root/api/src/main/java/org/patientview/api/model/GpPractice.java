package org.patientview.api.model;

/**
 * GpPractice, reduced set of data provided when creating a GP account
 * Created by jamesr@solidstategroup.com
 * Created on 08/02/2016
 */
public class GpPractice {

    private String name;
    private String code;
    private String url;

    public GpPractice() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
