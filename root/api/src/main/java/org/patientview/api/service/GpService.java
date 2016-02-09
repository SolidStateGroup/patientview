package org.patientview.api.service;

import net.lingala.zip4j.exception.ZipException;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.model.GpDetails;
import org.patientview.config.exception.VerificationException;

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

    @RoleOnly
    Map<String, String> updateMasterTable() throws IOException, ZipException;

    GpDetails validateDetails(GpDetails gpDetails) throws VerificationException;
}
