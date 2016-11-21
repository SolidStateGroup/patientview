package org.patientview.api.model;

import java.util.Date;

/**
 * ApiKey model represents api key data for front end
 */
public class ApiKey {

    private String key;
    private boolean expired;
    private Date expiryDate;

    public ApiKey(org.patientview.persistence.model.ApiKey key) {
        setKey(key.getKey());
        setExpiryDate(key.getExpiryDate());
        setExpired(key.getExpiryDate() != null && System.currentTimeMillis() > key.getExpiryDate().getTime());
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isExpired() {
        return expired;
    }

    private void setExpired(boolean expired) {
        this.expired = expired;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}
