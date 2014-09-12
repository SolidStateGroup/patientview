package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Observation;
import org.patientview.api.controller.BaseController;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.ObservationSummary;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.api.service.ObservationService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ObservationHeadingGroup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 03/09/2014
 */
@Service
public class ObservationServiceImpl extends BaseController<ObservationServiceImpl> implements ObservationService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupService groupService;

    @Inject
    private ObservationHeadingService observationHeadingService;

    private static final Logger LOG = LoggerFactory.getLogger(ObservationServiceImpl.class);

    @Override
    public List<FhirObservation> get(final Long userId, final String code, final String orderBy, final Long limit)
            throws ResourceNotFoundException, FhirResourceException {

        List<Observation> observations = new ArrayList<>();
        List<FhirObservation> fhirObservations = new ArrayList<>();

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        for (FhirLink fhirLink : user.getFhirLinks()) {
            StringBuilder query = new StringBuilder();
            query.append("SELECT  content::varchar ");
            query.append("FROM    observation ");
            query.append("WHERE   content->> 'subject' = '{\"display\": \"");
            query.append(fhirLink.getVersionId().toString());
            query.append("\", \"reference\": \"uuid\"}' ");

            if (StringUtils.isNotEmpty(code)) {
                query.append("AND content-> 'name' ->> 'text' = '");
                query.append(code);
                query.append("' ");
            }

            if (StringUtils.isNotEmpty(orderBy)) {
                query.append("ORDER BY content-> '");
                query.append(orderBy);
                query.append("' ");
            }

            if (StringUtils.isNotEmpty(orderBy)) {
                query.append("LIMIT ");
                query.append(limit);
            }

            observations.addAll(fhirResource.findResourceByQuery(query.toString(), Observation.class));
        }

        // convert to transport observations
        for (Observation observation : observations) {
            try {
                fhirObservations.add(new FhirObservation(observation));
            } catch (FhirResourceException fre) {
                LOG.debug(fre.getMessage());
            }
        }
        return fhirObservations;
    }

    @Override
    public List<Observation> get(final UUID patientUuid) {
        return null;
    }

    public List<ObservationSummary> getObservationSummary(Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<Group> groups = groupService.findGroupByUser(user);
        List<Group> specialties = new ArrayList<>();

        for (Group group : groups) {
            if (group.getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
                specialties.add(group);
            }
        }

        List<ObservationHeading> observationHeadings = observationHeadingService.findAll();
        List<ObservationSummary> observationData = new ArrayList<>();

        for (Group specialty : specialties) {
            observationData.add(getObservationSummary(specialty, observationHeadings, user));
        }
        return observationData;
    }

    private ObservationSummary getObservationSummary(Group group, List<ObservationHeading> observationHeadings,
                                                 User user) throws ResourceNotFoundException, FhirResourceException {

        ObservationSummary observationSummary = new ObservationSummary();
        observationSummary.setPanels(new HashMap<Long, List<org.patientview.api.model.ObservationHeading>>());
        observationSummary.setGroup(new org.patientview.api.model.Group(group));

        for (ObservationHeading observationHeading : observationHeadings) {

            // get panel and panel order for this specialty if available
            Long panel = getPanel(observationHeading, group);
            Long panelOrder = getPanelOrder(observationHeading, group);

            org.patientview.api.model.ObservationHeading summaryHeading =
                    buildSummaryHeading(panel, panelOrder, observationHeading);

            List<FhirObservation> latestObservations = get(user.getId(), observationHeading.getCode().toUpperCase(),
                    "appliesDateTime", 2L);

            if (!latestObservations.isEmpty()) {
                summaryHeading.setLatestObservation(latestObservations.get(0));

                if (latestObservations.size() > 1) {
                    summaryHeading.setValueChange(
                            latestObservations.get(0).getValue() - latestObservations.get(1).getValue());
                }
            }

            if (observationSummary.getPanels().get(panel) == null) {
                List<org.patientview.api.model.ObservationHeading> summaryHeadings = new ArrayList<>();
                summaryHeadings.add(summaryHeading);
                observationSummary.getPanels().put(panel, summaryHeadings);
            } else {
                observationSummary.getPanels().get(panel).add(summaryHeading);
            }
        }

        return observationSummary;
    }

    private Long getPanel(ObservationHeading observationHeading, Group group) {
        Long panel = observationHeading.getDefaultPanel();

        for (ObservationHeadingGroup observationHeadingGroup :
                observationHeading.getObservationHeadingGroups()) {
            if (observationHeadingGroup.getGroup().equals(group)) {
                panel = observationHeadingGroup.getPanel();
            }
        }
        return panel;
    }

    private Long getPanelOrder(ObservationHeading observationHeading, Group group) {
        Long panelOrder = observationHeading.getDefaultPanelOrder();

        for (ObservationHeadingGroup observationHeadingGroup :
                observationHeading.getObservationHeadingGroups()) {
            if (observationHeadingGroup.getGroup().equals(group)) {
                panelOrder = observationHeadingGroup.getPanelOrder();
            }
        }
        return panelOrder;
    }

    private org.patientview.api.model.ObservationHeading buildSummaryHeading(Long panel, Long panelOrder,
                                                                             ObservationHeading observationHeading) {
        org.patientview.api.model.ObservationHeading summaryHeading =
                new org.patientview.api.model.ObservationHeading();

        summaryHeading.setPanel(panel);
        summaryHeading.setPanelOrder(panelOrder);
        summaryHeading.setCode(observationHeading.getCode());
        summaryHeading.setHeading(observationHeading.getHeading());
        summaryHeading.setName(observationHeading.getName());
        summaryHeading.setNormalRange(observationHeading.getNormalRange());
        summaryHeading.setUnits(observationHeading.getUnits());
        summaryHeading.setMinGraph(observationHeading.getMinGraph());
        summaryHeading.setMaxGraph(observationHeading.getMaxGraph());
        summaryHeading.setInfoLink(observationHeading.getInfoLink());

        return summaryHeading;
    }
}
