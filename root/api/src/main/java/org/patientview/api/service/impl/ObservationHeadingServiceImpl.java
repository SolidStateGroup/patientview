package org.patientview.api.service.impl;

import org.patientview.api.service.ObservationHeadingService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Class to control the crud operations of the Observation Headings.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@Service
public class ObservationHeadingServiceImpl extends AbstractServiceImpl<ObservationHeadingServiceImpl>
        implements ObservationHeadingService {

    @Inject
    private ObservationHeadingRepository observationHeadingRepository;

    public Page<ObservationHeading> findAll(final GetParameters getParameters) {
        Pageable pageable = new PageRequest(0, Integer.MAX_VALUE);
        return observationHeadingRepository.findAll(pageable);
    }

    public ObservationHeading get(final Long observationHeadingId) {
        return observationHeadingRepository.findOne(observationHeadingId);
    }

    public ObservationHeading save(final ObservationHeading observationHeading) throws ResourceNotFoundException {
        return observationHeadingRepository.save(observationHeading);
    }

    public ObservationHeading add(final ObservationHeading observationHeading) {
        return observationHeadingRepository.save(observationHeading);
    }

    public void delete(final Long observationHeadingId) {
        observationHeadingRepository.delete(observationHeadingId);
    }
}
