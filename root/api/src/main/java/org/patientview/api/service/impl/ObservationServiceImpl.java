package org.patientview.api.service.impl;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Decimal;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.patientview.api.controller.BaseController;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.FhirDatabaseObservation;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.api.model.IdValue;
import org.patientview.api.model.ObservationSummary;
import org.patientview.api.model.UserResultCluster;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.api.service.ObservationService;
import org.patientview.api.service.PatientService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ObservationHeadingGroup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private PatientService patientService;

    @Inject
    private ObservationHeadingService observationHeadingService;

    private static final Logger LOG = LoggerFactory.getLogger(ObservationServiceImpl.class);
    private static final String COMMENT_RESULT_HEADING = "resultcomment";

    @Override
    public List<org.patientview.api.model.FhirObservation> get(final Long userId, final String code, final String orderBy,
                                     final String orderDirection, final Long limit)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<org.patientview.api.model.FhirObservation> fhirObservations = new ArrayList<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  content::varchar ");
                query.append("FROM    observation ");
                query.append("WHERE   content->> 'subject' = '{\"display\": \"");
                query.append(fhirLink.getResourceId().toString());
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
                        fhirObservation.setGroup(fhirGroup);
                    }
                    fhirObservations.add(new org.patientview.api.model.FhirObservation(fhirObservation));
                }
            }
        }

        return fhirObservations;
    }

    @Override
    public List<org.patientview.api.model.FhirObservation> getByFhirLinkAndCodes(final FhirLink fhirLink, final List<String> codes)
            throws ResourceNotFoundException, FhirResourceException {

        List<org.patientview.api.model.FhirObservation> fhirObservations = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT  content::varchar ");
        query.append("FROM    observation ");
        query.append("WHERE   content -> 'subject' ->> 'display' = '");
        query.append(fhirLink.getResourceId().toString());
        query.append("' ");

        if (!codes.isEmpty()) {
            query.append("AND content-> 'name' ->> 'text' IN (");

            for (int i = 0; i < codes.size(); i++) {

                query.append("'");
                query.append(codes.get(i));
                query.append("' ");

                if(i != codes.size()-1) {
                    query.append(",");
                }
            }
            query.append(") ");
        }

        LOG.info("patient 4a: " + new Date().getTime());

        List<Observation> observations = fhirResource.findResourceByQuery(query.toString(), Observation.class);

        LOG.info("patient 4b: " + new Date().getTime());

        // convert to transport observations
        for (Observation observation : observations) {
            FhirObservation fhirObservation = new FhirObservation(observation);
            Group fhirGroup = fhirLink.getGroup();
            if (fhirGroup != null) {
                fhirObservation.setGroup(fhirGroup);
            }
            fhirObservations.add(new org.patientview.api.model.FhirObservation(fhirObservation));
        }

        LOG.info("patient 4c: " + new Date().getTime());

        return fhirObservations;
    }

    // gets all latest observations in single query per fhirlink
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
                query.append("SELECT DISTINCT ON (2) ");
                query.append("CONTENT -> 'appliesDateTime', CONTENT -> 'name' -> 'text', ");
                query.append("CONTENT -> 'valueQuantity' -> 'value', ");
                query.append("CONTENT -> 'valueQuantity' -> 'comparator' ");
                query.append("FROM   observation ");
                query.append("WHERE  CONTENT -> 'subject' -> 'display' = '\"");
                query.append(fhirLink.getResourceId().toString());
                query.append("\"' ");
                query.append("ORDER  BY 2, 1 DESC");

                List<String[]> observationValues = fhirResource.findLatestObservationsByQuery(query.toString());

                // convert to transport observations
                for (String[] json : observationValues) {
                    if (!StringUtils.isEmpty(json[0])) {
                        try {
                            FhirObservation fhirObservation = new FhirObservation();

                            // remove timezone and parse date
                            String dateString = json[0].replace("\"", "");
                            XMLGregorianCalendar xmlDate
                                    = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString);
                            Date date = xmlDate.toGregorianCalendar().getTime();

                            fhirObservation.setApplies(date);
                            fhirObservation.setName(json[1].replace("\"", ""));
                            fhirObservation.setValue(json[2]);
                            if (StringUtils.isNotEmpty(json[3])) {
                                fhirObservation.setComparator(json[3].replace("\"", ""));
                            }
                            fhirObservation.setGroup(fhirLink.getGroup());

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

                        } catch (DatatypeConfigurationException e) {
                            LOG.error(e.getMessage());
                        }
                    }
                }
            }
        }

        return latestObservations;
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

        // this works and does retrieve difference between most recent and last observation, but is very slow
        /*Map<Long, List<FhirObservation>> latestObservations = new HashMap<>();
        for (ObservationHeading observationHeading : observationHeadings) {
            latestObservations.put(observationHeading.getId(), get(user.getId(),
                observationHeading.getCode().toUpperCase(), "appliesDateTime", "DESC", 2L));
        }

        for (Group specialty : specialties) {
            observationData.add(getObservationSummary(specialty, observationHeadings, latestObservations));
        }*/

        Map<String, FhirObservation> latestObservationMap = getLastObservations(user.getId());

        for (Group specialty : specialties) {
            observationData.add(getObservationSummaryMap(specialty, observationHeadings, latestObservationMap));
        }

        return observationData;
    }

    @Override
    public void addUserResultClusters(Long userId, List<UserResultCluster> userResultClusters)
            throws ResourceNotFoundException, FhirResourceException {

        User patientUser = userRepository.findOne(userId);
        if (patientUser == null) {
            throw new ResourceNotFoundException("User does not exist");
        }

        Group patientEnteredResultsGroup = groupService.findByCode(HiddenGroupCodes.PATIENT_ENTERED.toString());
        if (patientEnteredResultsGroup == null) {
            throw new ResourceNotFoundException("Group for patient entered results does not exist");
        }

        List<ObservationHeading> commentObservationHeadings
                = observationHeadingService.findByCode(COMMENT_RESULT_HEADING);
        if (CollectionUtils.isEmpty(commentObservationHeadings)) {
            throw new ResourceNotFoundException("Comment type observation heading does not exist");
        }

        if (CollectionUtils.isEmpty(patientUser.getIdentifiers())) {
            throw new ResourceNotFoundException("Patient must have at least one Identifier (NHS Number or other)");
        }

        // use first identifier for patient
        Identifier patientIdentifier = patientUser.getIdentifiers().iterator().next();

        // saves results, only if observation values are present or comment found
        for (UserResultCluster userResultCluster : userResultClusters) {

            DateTime applies = createDateTime(userResultCluster);

            List<Observation> fhirObservations = new ArrayList<>();

            // build observations
            for (IdValue idValue : userResultCluster.getValues()) {
                ObservationHeading observationHeading = observationHeadingService.get(idValue.getId());
                if (observationHeading == null) {
                    throw new ResourceNotFoundException("Observation Heading not found");
                }

                if (!idValue.getValue().isEmpty()) {
                    fhirObservations.add(buildObservation(applies, idValue.getValue(), null,
                            userResultCluster.getComments(), observationHeading));
                }
            }

            if (!fhirObservations.isEmpty()
                    || !(userResultCluster.getComments() == null || userResultCluster.getComments().isEmpty())) {

                // create FHIR Patient & fhirlink if not exists with PATIENT_ENTERED group, userId and identifier
                FhirLink fhirLink = getFhirLink(
                        patientEnteredResultsGroup, patientIdentifier.getIdentifier(), patientUser.getFhirLinks());

                if (fhirLink == null) {
                    Patient patient = patientService.buildPatient(patientUser, patientIdentifier);
                    JSONObject fhirPatient = fhirResource.create(patient);

                    // create FhirLink to link user to FHIR Patient at group PATIENT_ENTERED
                    fhirLink = new FhirLink();
                    fhirLink.setUser(patientUser);
                    fhirLink.setIdentifier(patientIdentifier);
                    fhirLink.setGroup(patientEnteredResultsGroup);
                    fhirLink.setResourceId(getResourceId(fhirPatient));
                    fhirLink.setVersionId(getVersionId(fhirPatient));
                    fhirLink.setResourceType(ResourceType.Patient.name());
                    fhirLink.setActive(true);

                    if (CollectionUtils.isEmpty(patientUser.getFhirLinks())) {
                        patientUser.setFhirLinks(new HashSet<FhirLink>());
                    }

                    patientUser.getFhirLinks().add(fhirLink);
                    userRepository.save(patientUser);
                }

                ResourceReference patientReference = Util.createFhirResourceReference(fhirLink.getResourceId());

                // store observations ready for native creation rather than fhir_create
                List<FhirDatabaseObservation> fhirDatabaseObservations = new ArrayList<>();

                // save observations
                for (Observation observation : fhirObservations) {
                    observation.setSubject(patientReference);

                    if (!(userResultCluster.getComments() == null || userResultCluster.getComments().isEmpty())) {
                        observation.setCommentsSimple(userResultCluster.getComments());
                    }

                    fhirDatabaseObservations.add(
                            new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation)));
                }

                // create comment observation based on patient entered comments
                if (!(userResultCluster.getComments() == null || userResultCluster.getComments().isEmpty())) {

                    Observation observation =
                        buildObservation(applies, userResultCluster.getComments(), null,
                                userResultCluster.getComments(), commentObservationHeadings.get(0));

                    observation.setSubject(patientReference);
                    fhirDatabaseObservations.add(
                            new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation)));
                }

                // now have collection, manually insert using native SQL
                if (!CollectionUtils.isEmpty(fhirDatabaseObservations)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("INSERT INTO observation ");
                    sb.append("(logical_id, version_id, resource_type, published, updated, content) VALUES ");

                    for (int i = 0; i < fhirDatabaseObservations.size() ; i++) {
                        FhirDatabaseObservation obs = fhirDatabaseObservations.get(i);
                        sb.append("(");
                        sb.append("'").append(obs.getLogicalId().toString()).append("','");
                        sb.append(obs.getVersionId().toString()).append("','");
                        sb.append(obs.getResourceType()).append("','");
                        sb.append(obs.getPublished().toString()).append("','");
                        sb.append(obs.getUpdated().toString()).append("','");
                        sb.append(obs.getContent());
                        sb.append("')");
                        if (i != (fhirDatabaseObservations.size() - 1)) {
                            sb.append(",");
                        }
                    }
                    fhirResource.executeSQL(sb.toString());
                }
            }
        }
    }

    @Override
    public FhirDatabaseObservation buildFhirDatabaseObservation(FhirObservation fhirObservation,
                              ObservationHeading observationHeading, FhirLink fhirLink)
            throws ResourceNotFoundException, FhirResourceException {

        // build actual FHIR observation and set subject
        Observation observation = buildObservation(createDateTime(fhirObservation.getApplies()),
                fhirObservation.getValue(), fhirObservation.getComparator(), fhirObservation.getComments(),
                observationHeading);

        observation.setSubject(Util.createFhirResourceReference(fhirLink.getResourceId()));

        // return new FhirDatabaseObservation with correct JSON content
        try {
            return new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation));
        } catch (NullArgumentException nae) {
            throw new FhirResourceException(nae.getMessage());
        }
    }

    private UUID getVersionId(final JSONObject bundle) {
        JSONArray resultArray = (JSONArray) bundle.get("entry");
        JSONObject resource = (JSONObject) resultArray.get(0);
        JSONArray links = (JSONArray) resource.get("link");
        JSONObject link = (JSONObject)  links.get(0);
        String[] href = link.getString("href").split("/");
        return UUID.fromString(href[href.length - 1]);
    }

    private UUID getResourceId(final JSONObject bundle) {
        JSONArray resultArray = (JSONArray) bundle.get("entry");
        JSONObject resource = (JSONObject) resultArray.get(0);
        return UUID.fromString(resource.get("id").toString());
    }

    private Observation buildObservation(DateTime applies, String value, String comparator, String comments,
                                         ObservationHeading observationHeading)
            throws FhirResourceException {

        Observation observation = new Observation();
        observation.setApplies(applies);
        observation.setReliability(new Enumeration<>(Observation.ObservationReliability.ok));
        observation.setStatusSimple(Observation.ObservationStatus.registered);

        try {
            Quantity quantity = new Quantity();
            quantity.setValue(createDecimal(value));
            quantity.setComparatorSimple(getComparator(comparator));
            quantity.setUnitsSimple(observationHeading.getUnits());
            observation.setValue(quantity);
        } catch (ParseException pe) {
            // parse exception, likely to be a string, e.g. comments store as text
            CodeableConcept comment = new CodeableConcept();
            comment.setTextSimple(value);
            comment.addCoding().setDisplaySimple(observationHeading.getHeading());
            observation.setValue(comment);
        }

        CodeableConcept name = new CodeableConcept();
        name.setTextSimple(observationHeading.getCode().toUpperCase());
        name.addCoding().setDisplaySimple(observationHeading.getHeading());

        observation.setName(name);
        observation.setIdentifier(createIdentifier(observationHeading.getCode()));
        observation.setCommentsSimple(comments);

        return observation;
    }

    private Quantity.QuantityComparator getComparator(String comparator) {

        if (StringUtils.isNotEmpty(comparator)) {
            if (comparator.contains(">=")) {
                return Quantity.QuantityComparator.greaterOrEqual;
            }

            if (comparator.contains("<=")) {
                return Quantity.QuantityComparator.lessOrEqual;
            }

            if (comparator.contains(">")) {
                return Quantity.QuantityComparator.greaterThan;
            }

            if (comparator.contains("<")) {
                return Quantity.QuantityComparator.lessThan;
            }
        }

        return null;
    }

    private DateTime createDateTime(UserResultCluster resultCluster) throws FhirResourceException {

        try {
            DateTime dateTime = new DateTime();
            DateAndTime dateAndTime = DateAndTime.now();
            dateAndTime.setYear(Integer.parseInt(resultCluster.getYear()));
            dateAndTime.setMonth(Integer.parseInt(resultCluster.getMonth()));
            dateAndTime.setDay(Integer.parseInt(resultCluster.getDay()));
            if (StringUtils.isNotEmpty(resultCluster.getHour())) {
                dateAndTime.setHour(Integer.parseInt(resultCluster.getHour()));
            } else {
                dateAndTime.setHour(0);
            }
            if (StringUtils.isNotEmpty(resultCluster.getMinute())) {
                dateAndTime.setMinute(Integer.parseInt(resultCluster.getMinute()));
            } else {
                dateAndTime.setMinute(0);
            }
            dateAndTime.setSecond(0);
            dateTime.setValue(dateAndTime);
            return dateTime;
        } catch (Exception e) {
            throw new FhirResourceException("Error converting date");
        }
    }

    private DateTime createDateTime(Date date) throws FhirResourceException {
        DateTime dateTime = new DateTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DateAndTime dateAndTime = new DateAndTime(calendar);
        dateTime.setValue(dateAndTime);
        return dateTime;
    }

    private Decimal createDecimal(String result) throws ParseException {
        Decimal decimal = new Decimal();

        // try parsing long, exception will be caught
        try {
            Long parsedLong = Long.parseLong(result);
        } catch (NumberFormatException nfe) {
            throw new ParseException(nfe.getMessage(), 0);
        }

        String resultString = result.replaceAll("[^.\\d]", "");
        NumberFormat decimalFormat = DecimalFormat.getInstance();

        try {
            if (StringUtils.isNotEmpty(resultString)) {
                decimal.setValue(BigDecimal.valueOf((decimalFormat.parse(resultString)).doubleValue()));
            }
        } catch (ParseException nfe) {
            throw new ParseException("Invalid value for observation", nfe.getErrorOffset());
        }

        return decimal;
    }

    private org.hl7.fhir.instance.model.Identifier createIdentifier(String code) {
        org.hl7.fhir.instance.model.Identifier identifier = new org.hl7.fhir.instance.model.Identifier();
        identifier.setLabelSimple("resultcode");
        identifier.setValueSimple(code);
        return identifier;
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
                            Double.parseDouble(latest.getValue())
                                - Double.parseDouble(observationList.get(1).getValue()));
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

    // note: doesn't return change since last observation, must be retrieved separately
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

    private FhirLink getFhirLink(Group group, String identifierText, Set<FhirLink> fhirLinks) {
        if (CollectionUtils.isEmpty(fhirLinks)) {
            return null;
        }

        for (FhirLink fhirLink : fhirLinks) {
            if (fhirLink.getGroup().equals(group) && fhirLink.getIdentifier().getIdentifier().equals(identifierText)) {
                return fhirLink;
            }
        }

        return null;
    }
}
