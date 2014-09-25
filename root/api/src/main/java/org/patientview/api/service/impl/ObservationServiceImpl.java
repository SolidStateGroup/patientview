package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    public List<FhirObservation> get(final Long userId, final String code, final String orderBy,
                                     final String orderDirection, final Long limit)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<FhirObservation> fhirObservations = new ArrayList<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
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

                if (StringUtils.isNotEmpty(orderDirection)) {
                    query.append(orderDirection);
                    query.append(" ");
                }

                if (StringUtils.isNotEmpty(orderBy)) {
                    query.append("LIMIT ");
                    query.append(limit);
                }

                List<Observation> observations = fhirResource.findResourceByQuery(query.toString(), Observation.class);

                // convert to transport observations
                for (Observation observation : observations) {
                    FhirObservation fhirObservation = new FhirObservation(observation);
                    Group fhirGroup = fhirLink.getGroup();
                    if (fhirGroup != null) {
                        fhirObservation.setGroup(new org.patientview.api.model.Group(fhirGroup));
                    }
                    fhirObservations.add(fhirObservation);
                }
            }
        }

        return fhirObservations;
    }

    // todo: develop optimised query (avoid search depth in json field?), this is actually slower
    private List<FhirObservation> getLastTwoObservations(final Long userId, final String code)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<FhirObservation> fhirObservations = new ArrayList<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  content -> 'valueQuantity' -> 'value' ");
                query.append("FROM    observation ");
                query.append("WHERE   content->> 'subject' = '{\"display\": \"");
                query.append(fhirLink.getVersionId().toString());
                query.append("\", \"reference\": \"uuid\"}' ");

                query.append("AND content-> 'name' = '{\"text\": \"");
                query.append(code.toUpperCase());
                query.append("\", \"coding\": [{\"display\": \"");
                query.append(WordUtils.capitalize(code));
                query.append("\"}]}' ");

                query.append("ORDER BY content-> 'appliesDateTime' ");
                query.append("DESC LIMIT 2 ");

                List<String> observationValues = fhirResource.findObservationValuesByQuery(query.toString());

                // convert to transport observations
                for (String json : observationValues) {
                    FhirObservation fhirObservation = new FhirObservation();
                    fhirObservation.setValue(Double.valueOf(json));
                    Group fhirGroup = fhirLink.getGroup();
                    if (fhirGroup != null) {
                        fhirObservation.setGroup(new org.patientview.api.model.Group(fhirGroup));
                    }
                    fhirObservations.add(fhirObservation);
                }
            }
        }

        return fhirObservations;
    }

    // todo: develop optimised query, gets all latest observations in single query per fhirlink
    private Map<String, FhirObservation> getLastObservations(final Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Map<String, FhirObservation> latestObservations = new HashMap<>();
        Map<String, Date> latestObservationDates = new HashMap<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT DISTINCT ON (2)");
                query.append("CONTENT -> 'appliesDateTime', CONTENT -> 'name' -> 'text', CONTENT -> 'valueQuantity' -> 'value' ");
                query.append("FROM   observation ");
                query.append("WHERE  CONTENT -> 'subject' -> 'display' = '\"");
                query.append(fhirLink.getVersionId().toString());
                query.append("\"' ");
                query.append("ORDER  BY 2, 1 DESC");

                List<String[]> observationValues = fhirResource.findLatestObservationsByQuery(query.toString());

                // convert to transport observations
                for (String[] json : observationValues) {
                    try {
                        FhirObservation fhirObservation = new FhirObservation();
                        Date date = new SimpleDateFormat("yyyy-MM-dd'T'hh':'mm':'ss.SSS'+01:00'", Locale.ENGLISH)
                                .parse(json[0].replace("\"", ""));

                        fhirObservation.setApplies(date);
                        fhirObservation.setName(json[1].replace("\"", ""));
                        fhirObservation.setValue(Double.valueOf(json[2]));
                        fhirObservation.setGroup(new org.patientview.api.model.Group(fhirLink.getGroup()));

                        String code = json[1].replace("\"", "").toUpperCase();

                        if (latestObservationDates.get(code) != null) {
                            if (latestObservationDates.get(code).getTime() < date.getTime()) {
                                latestObservations.put(code, fhirObservation);
                                latestObservationDates.put(code, date);
                            }
                        } else {
                            latestObservations.put(code, fhirObservation);
                            latestObservationDates.put(code, date);
                        }

                    } catch (ParseException e) {
                        LOG.error(e.getMessage());
                    }
                }
            }
        }

        return latestObservations;
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

        // this works but is slow
        /*Map<Long, List<FhirObservation>> latestObservations = new HashMap<>();
        for (ObservationHeading observationHeading : observationHeadings) {
            latestObservations.put(observationHeading.getId(), get(user.getId(), observationHeading.getCode().toUpperCase(), "appliesDateTime", "DESC", 2L));
        }

        for (Group specialty : specialties) {
            observationData.add(getObservationSummary(specialty, observationHeadings, latestObservations));
        }*/

        // todo: testing (doesn't return change)
        Map<String, FhirObservation> latestObservationMap = getLastObservations(user.getId());

        for (Group specialty : specialties) {
            observationData.add(getObservationSummaryMap(specialty, observationHeadings, latestObservationMap));
        }

        return observationData;
    }

    private ObservationSummary getObservationSummary(Group group, List<ObservationHeading> observationHeadings,
         Map<Long, List<FhirObservation>> latestObservations) throws ResourceNotFoundException, FhirResourceException {

        ObservationSummary observationSummary = new ObservationSummary();
        observationSummary.setPanels(new HashMap<Long, List<org.patientview.api.model.ObservationHeading>>());
        observationSummary.setGroup(new org.patientview.api.model.Group(group));

        for (ObservationHeading observationHeading : observationHeadings) {

            // get panel and panel order for this specialty if available, otherwise use default
            Long panel = observationHeading.getDefaultPanel();
            Long panelOrder = observationHeading.getDefaultPanelOrder();

            for (ObservationHeadingGroup observationHeadingGroup : observationHeading.getObservationHeadingGroups()) {
                if (observationHeadingGroup.getGroup().getId().equals(group.getId())) {
                    panel = observationHeadingGroup.getPanel();
                    panelOrder = observationHeadingGroup.getPanelOrder();
                }
            }

            // don't include any observation heading with panel = 0
            if (panel != null && panel != 0L) {

                // create transport observation heading
                org.patientview.api.model.ObservationHeading transportObservationHeading =
                    buildSummaryHeading(panel, panelOrder, observationHeading);

                // add latest observation and value changed to transport observation heading if present
                List<FhirObservation> observationList = latestObservations.get(observationHeading.getId());

                if (!observationList.isEmpty()) {
                    FhirObservation latest = observationList.get(0);
                    transportObservationHeading.setLatestObservation(latest);

                    if (observationList.size() > 1) {
                        transportObservationHeading.setValueChange(
                            latest.getValue() - observationList.get(1).getValue());
                    }
                }

                // add panel if not present and add transport observation heading
                if (observationSummary.getPanels().get(panel) == null) {
                    List<org.patientview.api.model.ObservationHeading> summaryHeadings = new ArrayList<>();
                    summaryHeadings.add(transportObservationHeading);
                    observationSummary.getPanels().put(panel, summaryHeadings);
                } else {
                    observationSummary.getPanels().get(panel).add(transportObservationHeading);
                }
            }
        }

        return observationSummary;
    }

    private ObservationSummary getObservationSummaryMap(Group group, List<ObservationHeading> observationHeadings,
        Map<String, FhirObservation> latestObservations) throws ResourceNotFoundException, FhirResourceException {

        ObservationSummary observationSummary = new ObservationSummary();
        observationSummary.setPanels(new HashMap<Long, List<org.patientview.api.model.ObservationHeading>>());
        observationSummary.setGroup(new org.patientview.api.model.Group(group));

        for (ObservationHeading observationHeading : observationHeadings) {

            // get panel and panel order for this specialty if available, otherwise use default
            Long panel = observationHeading.getDefaultPanel();
            Long panelOrder = observationHeading.getDefaultPanelOrder();

            for (ObservationHeadingGroup observationHeadingGroup : observationHeading.getObservationHeadingGroups()) {
                if (observationHeadingGroup.getGroup().getId().equals(group.getId())) {
                    panel = observationHeadingGroup.getPanel();
                    panelOrder = observationHeadingGroup.getPanelOrder();
                }
            }

            // don't include any observation heading with panel = 0
            if (panel != null && panel != 0L) {

                // create transport observation heading
                org.patientview.api.model.ObservationHeading transportObservationHeading =
                    buildSummaryHeading(panel, panelOrder, observationHeading);

                // add latest observation
                transportObservationHeading.setLatestObservation(
                        latestObservations.get(observationHeading.getCode().toUpperCase()));

                // add panel if not present and add transport observation heading
                if (observationSummary.getPanels().get(panel) == null) {
                    List<org.patientview.api.model.ObservationHeading> summaryHeadings = new ArrayList<>();
                    summaryHeadings.add(transportObservationHeading);
                    observationSummary.getPanels().put(panel, summaryHeadings);
                } else {
                    observationSummary.getPanels().get(panel).add(transportObservationHeading);
                }
            }
        }

        return observationSummary;
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
