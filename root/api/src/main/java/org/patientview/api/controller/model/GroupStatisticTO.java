package org.patientview.api.controller.model;

import java.math.BigInteger;
import java.util.Date;

/**
 * TODO refactor the others classes into ...TO or put in a different package
 *
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
public class GroupStatisticTO implements Comparable {

    private Date startDate;
    private Date endDate;

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
    private BigInteger countOfUserInactive = BigInteger.ZERO;
    private BigInteger countOfUserLocked = BigInteger.ZERO;

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

    public BigInteger getCountOfUserInactive() {
        return countOfUserInactive;
    }

    public void setCountOfUserInactive(BigInteger countOfUserInactive) {
        this.countOfUserInactive = countOfUserInactive;
    }

    public BigInteger getCountOfUserLocked() {
        return countOfUserLocked;
    }

    public void setCountOfUserLocked(BigInteger countOfUserLocked) {
        this.countOfUserLocked = countOfUserLocked;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public int compareTo(Object o) {
        if (o != null && o instanceof GroupStatisticTO) {
            GroupStatisticTO groupStatisticTO = (GroupStatisticTO) o;
            if (this.getStartDate().getTime() < groupStatisticTO.getStartDate().getTime()) {
                return -1;
            } else if (this.getStartDate().getTime() > groupStatisticTO.getStartDate().getTime()) {
                return 1;
            } else {
                return 0;
            }
        }
        return -1;
    }
}
