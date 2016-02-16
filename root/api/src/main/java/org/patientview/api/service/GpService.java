package org.patientview.api.service;

import net.lingala.zip4j.exception.ZipException;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.GpDetails;

import java.io.IOException;
import java.util.Map;

/**
 * GP Service, for managing master table of GPs from external sources
 *
 * Created by jamesr@solidstategroup.com
 * Created on 02/01/2016
 */
public interface GpService {

    GpDetails claim(GpDetails gpDetails) throws VerificationException;

    @UserOnly
    void invite(Long userId, FhirPatient fhirPatient) throws VerificationException;

    @RoleOnly
    Map<String, String> updateMasterTable() throws IOException, ZipException;

    GpDetails validateDetails(GpDetails gpDetails) throws VerificationException;
}
