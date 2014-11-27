package org.patientview.importer.manager.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.Utility.Util;
import org.patientview.importer.exception.ImportResourceException;
import org.patientview.importer.manager.ImportManager;
import org.patientview.importer.service.AllergyService;
import org.patientview.importer.service.AuditService;
import org.patientview.importer.service.ConditionService;
import org.patientview.importer.service.DiagnosticService;
import org.patientview.importer.service.DocumentReferenceService;
import org.patientview.importer.service.EncounterService;
import org.patientview.importer.service.MedicationService;
import org.patientview.importer.service.ObservationService;
import org.patientview.importer.service.OrganizationService;
import org.patientview.importer.service.PatientService;
import org.patientview.importer.service.PractitionerService;
import org.patientview.importer.service.impl.AbstractServiceImpl;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.SQLException;
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
    private PatientService patientService;

    @Inject
    private ObservationService observationService;

    @Inject
    private ConditionService conditionService;

    @Inject
    private PractitionerService practitionerService;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private EncounterService encounterService;

    @Inject
    private MedicationService medicationService;

    @Inject
    private DiagnosticService diagnosticService;

    @Inject
    private DocumentReferenceService documentReferenceService;

    @Inject
    private AllergyService allergyService;

    @Inject
    private AuditService auditService;

    @Override
    public void validate(Patientview patientview) throws ImportResourceException {

        // Patient exists with this identifier
        try {
            patientService.matchPatientByIdentifierValue(patientview);
        } catch (ResourceNotFoundException rnf) {
            String errorMessage = patientview.getPatient().getPersonaldetails().getNhsno()
                    + ": patient with identifier does not exist in PatientView";
            LOG.error(errorMessage);
            throw new ImportResourceException(errorMessage);
        }

        // Group exists
        if (!organizationService.groupWithCodeExists(patientview.getCentredetails().getCentrecode())) {
            String errorMessage = patientview.getCentredetails().getCentrecode()
                    + ": group does not exist in PatientView";
            LOG.error(errorMessage);
            throw new ImportResourceException(errorMessage);
        }
    }

    @Override
    public void process(Patientview patientview, String xml, Long importerUserId) throws ImportResourceException {

        ResourceReference practitionerReference;
        ResourceReference organizationReference;

        try {
            Date start = new Date();
            LOG.info("Starting to process data for NHS number: "
                    + patientview.getPatient().getPersonaldetails().getNhsno());

            // update Organization based on <centrecode> (Unit/centre details)
            UUID organizationUuid = organizationService.add(patientview);
            organizationReference = Util.createResourceReference(organizationUuid);

            // update Practitioner based on <gpname> (GP details)
            UUID practitionerUuid = practitionerService.add(patientview);
            practitionerReference = Util.createResourceReference(practitionerUuid);

            // update core Patient object based on <nhsno>
            FhirLink fhirLink = patientService.add(patientview, practitionerReference);

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
            LOG.info("Finished processing data for NHS number: "
                    + patientview.getPatient().getPersonaldetails().getNhsno()
                    + ". Took " + getDateDiff(start,end,TimeUnit.SECONDS) + " seconds.");

            createAudit(AuditActions.PATIENT_DATA_SUCCESS, patientview.getPatient().getPersonaldetails().getNhsno(),
                    patientview.getCentredetails().getCentrecode(), null, xml, importerUserId);

        } catch (FhirResourceException | ResourceNotFoundException | SQLException e) {
            LOG.error(patientview.getPatient().getPersonaldetails().getNhsno()
                    + ": unable to build patient. Message: " + e.getMessage());

            throw new ImportResourceException(patientview.getPatient().getPersonaldetails().getNhsno()
                    + ": " + e.getMessage());
        }
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }


    private void createAudit(AuditActions auditActions, String identifier, String unitCode,
                             String information, String xml, Long importerUserId) {

        Audit audit = new Audit();
        audit.setAuditActions(auditActions);
        audit.setActorId(importerUserId);
        audit.setInformation(information);
        audit.setXml(xml);

        // attempt to set identifier and user being imported from identifier
        if (identifier != null) {
            audit.setIdentifier(identifier);
            User patientUser = auditService.getUserByIdentifier(identifier);
            if (patientUser != null) {
                audit.setSourceObjectId(patientUser.getId());
                audit.setSourceObjectType(AuditObjectTypes.User);
                audit.setUsername(patientUser.getUsername());
            }
        }

        // attempt to set group doing the importing
        if (unitCode != null) {
            Group group = auditService.getGroupByCode(unitCode);
            if (group != null) {
                audit.setGroup(group);
            }
        }

        auditService.save(audit);
    }
}
