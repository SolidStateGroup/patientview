package org.patientview.api.model;

public class RequeueReport {

    private final int count;

    public RequeueReport(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
