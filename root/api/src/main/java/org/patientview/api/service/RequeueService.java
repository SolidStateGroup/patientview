package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.model.RequeueReport;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.util.Date;

/**
 * Provides the ability to rebuild and requeue messages.
 */
public interface RequeueService {

    /**
     *
     * @param start Start time to query for survey responses
     * @param end   End time to query for survey responses
     * @return      Report on how many surveys were requeued
     * @throws JAXBException
     * @throws DatatypeConfigurationException
     */
    @RoleOnly(roles = { RoleName.GLOBAL_ADMIN })
    @Transactional
    RequeueReport xkrdcSurveys(Date start, Date end, Long userId) throws JAXBException, DatatypeConfigurationException;
}
