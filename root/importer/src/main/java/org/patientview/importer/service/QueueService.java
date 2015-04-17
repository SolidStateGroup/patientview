package org.patientview.importer.service;

import generated.Patientview;
import org.patientview.importer.exception.ImportResourceException;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
public interface QueueService {

    public void importRecord(Patientview patientview) throws ImportResourceException;
}
