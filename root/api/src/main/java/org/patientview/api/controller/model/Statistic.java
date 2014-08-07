package org.patientview.api.controller.model;

/**
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
public class Statistic {

    private Long countOfPatients;
    private Long countOfLogons;
    private Long countOfUniqueLogons;
    private Long countOfPatientViews;
    private Long countOfPatientAdds;
    private Long countOfImportFails;
    private Long countOfImportLoads;
    private Long countOfPasswordChanges;
    private Long countOfPasswordLocks;
    private Long countOfPatientRemoveDatas;
    private Long countOfPatientDeletes;

    public Long getCountOfPatients() {
        return countOfPatients;
    }

    public void setCountOfPatients(final Long countOfPatients) {
        this.countOfPatients = countOfPatients;
    }

    public Long getCountOfLogons() {
        return countOfLogons;
    }

    public void setCountOfLogons(final Long countOfLogons) {
        this.countOfLogons = countOfLogons;
    }

    public Long getCountOfUniqueLogons() {
        return countOfUniqueLogons;
    }

    public void setCountOfUniqueLogons(final Long countOfUniqueLogons) {
        this.countOfUniqueLogons = countOfUniqueLogons;
    }

    public Long getCountOfPatientViews() {
        return countOfPatientViews;
    }

    public void setCountOfPatientViews(final Long countOfPatientViews) {
        this.countOfPatientViews = countOfPatientViews;
    }

    public Long getCountOfPatientAdds() {
        return countOfPatientAdds;
    }

    public void setCountOfPatientAdds(final Long countOfPatientAdds) {
        this.countOfPatientAdds = countOfPatientAdds;
    }

    public Long getCountOfImportFails() {
        return countOfImportFails;
    }

    public void setCountOfImportFails(final Long countOfImportFails) {
        this.countOfImportFails = countOfImportFails;
    }

    public Long getCountOfImportLoads() {
        return countOfImportLoads;
    }

    public void setCountOfImportLoads(final Long countOfImportLoads) {
        this.countOfImportLoads = countOfImportLoads;
    }

    public Long getCountOfPasswordChanges() {
        return countOfPasswordChanges;
    }

    public void setCountOfPasswordChanges(final Long countOfPasswordChanges) {
        this.countOfPasswordChanges = countOfPasswordChanges;
    }

    public Long getCountOfPasswordLocks() {
        return countOfPasswordLocks;
    }

    public void setCountOfPasswordLocks(final Long countOfPasswordLocks) {
        this.countOfPasswordLocks = countOfPasswordLocks;
    }

    public Long getCountOfPatientRemoveDatas() {
        return countOfPatientRemoveDatas;
    }

    public void setCountOfPatientRemoveDatas(final Long countOfPatientRemoveDatas) {
        this.countOfPatientRemoveDatas = countOfPatientRemoveDatas;
    }

    public Long getCountOfPatientDeletes() {
        return countOfPatientDeletes;
    }

    public void setCountOfPatientDeletes(final Long countOfPatientDeletes) {
        this.countOfPatientDeletes = countOfPatientDeletes;
    }
}
