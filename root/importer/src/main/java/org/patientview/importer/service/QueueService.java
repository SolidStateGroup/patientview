package org.patientview.importer.service;

import generated.Patientview;
import org.patientview.config.exception.ImportResourceException;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
public interface QueueService {
    void importRecord(Patientview patientview) throws ImportResourceException;
}
