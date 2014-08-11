package org.patientview.api.controller.model;

import java.math.BigInteger;

/**
 * TODO refactor the others classes into ...TO or put in a different package
 *
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
public class GroupStatisticTO {

    private BigInteger countOfPatients = BigInteger.ZERO;
    private BigInteger countOfLogons = BigInteger.ZERO;
    private BigInteger countOfUniqueLogons = BigInteger.ZERO;
    private BigInteger countOfPatientViews = BigInteger.ZERO;
    private BigInteger countOfPatientAdds = BigInteger.ZERO;
    private BigInteger countOfImportFails = BigInteger.ZERO;
    private BigInteger countOfImportLoads = BigInteger.ZERO;
    private BigInteger countOfPasswordChanges = BigInteger.ZERO;
    private BigInteger countOfAccountLocks = BigInteger.ZERO;
    private BigInteger countOfPatientRemoves = BigInteger.ZERO;
    private BigInteger countOfPatientDeletes = BigInteger.ZERO;

    public BigInteger getCountOfPatients() {
        return countOfPatients;
    }

    public void setCountOfPatients(final BigInteger countOfPatients) {
        this.countOfPatients = countOfPatients;
    }

    public BigInteger getCountOfLogons() {
        return countOfLogons;
    }

    public void setCountOfLogons(final BigInteger countOfLogons) {
        this.countOfLogons = countOfLogons;
    }

    public BigInteger getCountOfUniqueLogons() {
        return countOfUniqueLogons;
    }

    public void setCountOfUniqueLogons(final BigInteger countOfUniqueLogons) {
        this.countOfUniqueLogons = countOfUniqueLogons;
    }

    public BigInteger getCountOfPatientViews() {
        return countOfPatientViews;
    }

    public void setCountOfPatientViews(final BigInteger countOfPatientViews) {
        this.countOfPatientViews = countOfPatientViews;
    }

    public BigInteger getCountOfPatientAdds() {
        return countOfPatientAdds;
    }

    public void setCountOfPatientAdds(final BigInteger countOfPatientAdds) {
        this.countOfPatientAdds = countOfPatientAdds;
    }

    public BigInteger getCountOfImportFails() {
        return countOfImportFails;
    }

    public void setCountOfImportFails(final BigInteger countOfImportFails) {
        this.countOfImportFails = countOfImportFails;
    }

    public BigInteger getCountOfImportLoads() {
        return countOfImportLoads;
    }

    public void setCountOfImportLoads(final BigInteger countOfImportLoads) {
        this.countOfImportLoads = countOfImportLoads;
    }

    public BigInteger getCountOfPasswordChanges() {
        return countOfPasswordChanges;
    }

    public void setCountOfPasswordChanges(final BigInteger countOfPasswordChanges) {
        this.countOfPasswordChanges = countOfPasswordChanges;
    }

    public BigInteger getCountOfAccountLocks() {
        return countOfAccountLocks;
    }

    public void setCountOfAccountLocks(final BigInteger countOfAccountLocks) {
        this.countOfAccountLocks = countOfAccountLocks;
    }

    public BigInteger getCountOfPatientRemoves() {
        return countOfPatientRemoves;
    }

    public void setCountOfPatientRemoves(final BigInteger countOfPatientRemoves) {
        this.countOfPatientRemoves = countOfPatientRemoves;
    }

    public BigInteger getCountOfPatientDeletes() {
        return countOfPatientDeletes;
    }

    public void setCountOfPatientDeletes(final BigInteger countOfPatientDeletes) {
        this.countOfPatientDeletes = countOfPatientDeletes;
    }
}
