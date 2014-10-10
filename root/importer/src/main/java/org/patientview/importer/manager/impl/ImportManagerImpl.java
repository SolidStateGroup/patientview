package org.patientview.importer.manager.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.exception.ImportResourceException;
import org.patientview.importer.manager.ImportManager;
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
import org.patientview.importer.Util.Util;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
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
    private Properties properties;


    @Override
    public boolean validate(Patientview patientview) {

        // patient exists
        try {
            patientService.matchPatientByIdentifierValue(patientview);
        } catch (ResourceNotFoundException rnf) {
            return false;
        }

        // organization/unit/group exists
        if ((patientview.getCentredetails() == null) ||
                !organizationService.groupWithCodeExists(patientview.getCentredetails().getCentrecode())) {
            return false;
        }

        return true;
    }

    @Override
    public void process(Patientview patientview) throws ImportResourceException {

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
            // Observation of type NonTestObservationTypes.BLOOD_GROUP
            observationService.add(patientview, fhirLink);

            // add Condition, deleting existing (diagnoses)
            conditionService.add(patientview, fhirLink);

            // Add Encounter, deleting existing (used for treatment and transplant status)
            encounterService.add(patientview, fhirLink, organizationReference);

            // Add MedicationStatement and associated Medication, deleting existing (drugdetails)
            medicationService.add(patientview, fhirLink);

            // Add DiagnosticReport and associated Observation (diagnostics, originally IBD now generic)
            diagnosticService.add(patientview, fhirLink);

            // Add DocumentReference, deleting those with the same date (letters)
            documentReferenceService.add(patientview, fhirLink);

            Date end = new Date();
            LOG.info("Finished processing data for NHS number: "
                    + patientview.getPatient().getPersonaldetails().getNhsno()
                    + ". Took " + getDateDiff(start,end,TimeUnit.SECONDS) + " seconds.");

        } catch (FhirResourceException | ResourceNotFoundException | SQLException e) {
            LOG.error("Unable to build patient " + patientview.getPatient().getPersonaldetails().getNhsno()
                + ". Message: " + e.getMessage());
            throw new ImportResourceException("Could not process patient data");
        }
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }
}
