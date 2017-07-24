package org.patientview.importer.manager.impl;

import generated.Patientview;
import generated.Survey;
import generated.SurveyResponse;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.manager.ImportManager;
import org.patientview.importer.service.impl.AbstractServiceImpl;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.service.AllergyService;
import org.patientview.service.AuditService;
import org.patientview.service.ConditionService;
import org.patientview.service.DiagnosticService;
import org.patientview.service.DocumentReferenceService;
import org.patientview.service.EncounterService;
import org.patientview.service.GpLetterService;
import org.patientview.service.MedicationService;
import org.patientview.service.ObservationService;
import org.patientview.service.OrganizationService;
import org.patientview.service.PatientService;
import org.patientview.service.PractitionerService;
import org.patientview.service.SurveyResponseService;
import org.patientview.service.SurveyService;
import org.patientview.service.UkrdcService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;
import uk.org.rixg.PatientRecord;

import javax.inject.Inject;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Service
public class ImportManagerImpl extends AbstractServiceImpl<ImportManager> implements ImportManager {

    @Inject
    private AllergyService allergyService;

    @Inject
    private AuditService auditService;

    @Inject
    private ConditionService conditionService;

    @Inject
    private DiagnosticService diagnosticService;

    @Inject
    private DocumentReferenceService documentReferenceService;

    @Inject
    private EncounterService encounterService;

    @Inject
    private GpLetterService gpLetterService;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private MedicationService medicationService;

    @Inject
    private ObservationService observationService;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private PatientService patientService;

    @Inject
    private PractitionerService practitionerService;

    @Inject
    private SurveyService surveyService;

    @Inject
    private SurveyResponseService surveyResponseService;

    @Inject
    private UkrdcService ukrdcService;

    @Override
    public void process(PatientRecord patientRecord, String xml, Long importerUserId)
            throws ImportResourceException {

        String identifier = null;
        try {
            // attempt to get identifier if exists, used by audit
            identifier = ukrdcService.findIdentifier(patientRecord);

            ukrdcService.process(patientRecord, xml, identifier, importerUserId);
            LOG.info(identifier + ": UKRDC PatientRecord processed");
        } catch (Exception e) {
            LOG.error(identifier + ": UKRDC PatientRecord process error", e);
            throw new ImportResourceException(e.getMessage());
        }
    }

    @Override
    public void process(Patientview patientview, String xml, Long importerUserId) throws ImportResourceException {

        ResourceReference practitionerReference = null;
        ResourceReference organizationReference;

        try {
            Date start = new Date();
            LOG.info(patientview.getPatient().getPersonaldetails().getNhsno() + ": Starting Import");

            // update Organization based on <centrecode> (Unit/centre details)
            UUID organizationUuid = organizationService.add(patientview);
            organizationReference = Util.createResourceReference(organizationUuid);

            // update Practitioner based on <gpname> (GP details)
            UUID practitionerUuid = practitionerService.add(patientview);
            if (practitionerUuid != null) {
                practitionerReference = Util.createResourceReference(practitionerUuid);
            }

            // update core Patient object based on <nhsno>
            FhirLink fhirLink = patientService.add(patientview, practitionerReference);

            try {
                gpLetterService.createGpLetter(fhirLink, patientview);
            } catch (Exception e) {
                LOG.info("Could not create GP letter, continuing: " + e.getMessage());
            }

            // add other practitioners, only used by IBD for named consultant and nurse
            practitionerService.addOtherPractitionersToPatient(patientview, fhirLink);

            // add Observation, deleting existing Observation within <test><daterange> (tests) and existing
            // Observation of type NonTestObservationTypes.BLOOD_GROUP, PTPULSE, DPPULSE
            observationService.add(patientview, fhirLink);

            // add Condition, deleting existing (diagnoses)
            conditionService.add(patientview, fhirLink);

            // add Encounter, deleting existing (used for treatment and transplant status)
            encounterService.add(patientview, fhirLink, organizationReference);

            // add MedicationStatement and associated Medication, deleting existing (drugdetails)
            medicationService.add(patientview, fhirLink);

            // add DiagnosticReport and associated Observation (diagnostics, originally IBD now generic)
            diagnosticService.add(patientview, fhirLink);

            // add DocumentReference, deleting those with the same date (letters)
            documentReferenceService.add(patientview, fhirLink);

            // add AllergyIntolerance, Substance and AdverseReaction (allergy), deleting existing
            allergyService.add(patientview, fhirLink);

            Date end = new Date();
            LOG.info(patientview.getPatient().getPersonaldetails().getNhsno()
                    + ": Finished Import. Took " + getDateDiff(start,end,TimeUnit.SECONDS) + " seconds.");

            auditService.createAudit(AuditActions.PATIENT_DATA_SUCCESS,
                    patientview.getPatient().getPersonaldetails().getNhsno(),
                    patientview.getCentredetails().getCentrecode(), null, xml, importerUserId);

            updateGroupLastImportDate(patientview.getCentredetails().getCentrecode());

        } catch (Exception e) {
            LOG.error(patientview.getPatient().getPersonaldetails().getNhsno()
                    + ": Error importing patient. Message: " + e.getMessage(), e);

            throw new ImportResourceException(patientview.getPatient().getPersonaldetails().getNhsno()
                    + ": Error, " + e.getMessage());
        }
    }

    @Override
    public void process(Survey survey, String xml, Long importerUserId) throws ImportResourceException {
        try {
            surveyService.add(survey);
            LOG.info("Survey setup data type '" + survey.getType() + "' added");

            // audit
            auditService.createAudit(AuditActions.SURVEY_SUCCESS, null, null, null, xml, importerUserId);
        } catch (Exception e) {
            LOG.error("Survey setup data type '" + survey.getType() + "' process error", e);

            // audit
            auditService.createAudit(AuditActions.SURVEY_FAIL, null, null, null, xml, importerUserId);

            throw new ImportResourceException(e.getMessage());
        }
    }

    @Override
    public void process(SurveyResponse surveyResponse, String xml, Long importerUserId) throws ImportResourceException {
        try {
            surveyResponseService.add(surveyResponse);
            LOG.info(surveyResponse.getIdentifier() + ": survey response type '" + surveyResponse.getSurveyType()
                    + "' added");

            // audit
            auditService.createAudit(AuditActions.SURVEY_RESPONSE_SUCCESS, surveyResponse.getIdentifier(), null, null,
                    xml, importerUserId);
        } catch (Exception e) {
            LOG.error(surveyResponse.getIdentifier() + ": survey response type '" + surveyResponse.getSurveyType()
                    + "' process error");

            // audit
            auditService.createAudit(AuditActions.SURVEY_RESPONSE_FAIL,
                    surveyResponse.getIdentifier(), null, null, xml, importerUserId);

            throw new ImportResourceException(e.getMessage());
        }
    }

    @Override
    public void validate(Patientview patientview) throws ImportResourceException {
        // Patient exists with this identifier
        try {
            patientService.matchPatientByIdentifierValue(patientview);
        } catch (ResourceNotFoundException rnf) {
            throw new ImportResourceException("Patient with identifier '"
                    + patientview.getPatient().getPersonaldetails().getNhsno() + "' does not exist in PatientView");
        }

        // Group exists
        if (!organizationService.groupWithCodeExists(patientview.getCentredetails().getCentrecode())) {
            throw new ImportResourceException("Group with code '" + patientview.getCentredetails().getCentrecode()
                    + "' does not exist in PatientView");
        }
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }
    
    private void updateGroupLastImportDate(String groupCode) {
        Group group = groupRepository.findByCode(groupCode);
        if (group != null) {
            group.setLastImportDate(new Date());
            groupRepository.save(group);
        }
    }
}
