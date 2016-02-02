package org.patientview.api.service;

import net.lingala.zip4j.exception.ZipException;
import org.patientview.api.annotation.RoleOnly;

import java.io.IOException;

/**
 * GP Service, for managing master table of GPs from external sources
 *
 * Created by jamesr@solidstategroup.com
 * Created on 02/01/2016
 */
public interface GpService {

    @RoleOnly
    String updateMasterTable() throws IOException, ZipException;
}
