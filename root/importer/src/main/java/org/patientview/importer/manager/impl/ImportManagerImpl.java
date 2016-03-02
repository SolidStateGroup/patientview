package org.patientview.importer.manager.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.Utility.Util;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.importer.manager.ImportManager;
import org.patientview.service.AllergyService;
import org.patientview.importer.service.AuditService;
import org.patientview.service.ConditionService;
import org.patientview.service.DiagnosticService;
import org.patientview.service.DocumentReferenceService;
import org.patientview.service.EncounterService;
import org.patientview.importer.service.GpLetterService;
import org.patientview.importer.service.GroupRoleService;
import org.patientview.service.MedicationService;
import org.patientview.service.ObservationService;
import org.patientview.service.OrganizationService;
import org.patientview.service.PatientService;
import org.patientview.service.PractitionerService;
import org.patientview.importer.service.impl.AbstractServiceImpl;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
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
    private GroupRoleService groupRoleService;

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

    @Override
    public void createGpLetter(FhirLink fhirLink, Patientview patientview) throws ResourceNotFoundException {
        // verbose logging
        LOG.info("fhirLink.isNew(): " + fhirLink.isNew());
        LOG.info("hasValidPracticeDetails(): " + gpLetterService.hasValidPracticeDetails(patientview));
        LOG.info("hasValidPracticeDetailsSingleMaster(): "
                + gpLetterService.hasValidPracticeDetailsSingleMaster(patientview));

        // check FhirLink is new and GP details are suitable for using in GP letter table (either enough details
        // or only have postcode but no more than one in Gp master table)
        if (fhirLink.isNew()
                && (gpLetterService.hasValidPracticeDetails(patientview)
                    || gpLetterService.hasValidPracticeDetailsSingleMaster(patientview))) {
            // check if any entries exist matching GP details in GP letter table
            List<GpLetter> gpLetters = gpLetterService.matchByGpDetails(patientview);

            // verbose logging
            LOG.info("gpLetters.size(): " + gpLetters.size());

            if (!CollectionUtils.isEmpty(gpLetters)) {
                // match exists, check if first entry is claimed (all will be claimed if so)
                if (gpLetters.get(0).getClaimedDate() != null && gpLetters.get(0).getClaimedGroup() != null) {
                    LOG.info("gpLetters(0) is claimed, add group role for group "
                            + gpLetters.get(0).getClaimedGroup().getCode());

                    // add GroupRole for this patient and GP group
                    groupRoleService.add(
                            fhirLink.getUser().getId(), gpLetters.get(0).getClaimedGroup().getId(), RoleType.PATIENT);
                } else {
                    LOG.info("gpLetters(0) is not claimed, checking gp name is unique");

                    // entries exist but not claimed, check GP name against existing GP letter entries
                    boolean gpNameExists = false;
                    for (GpLetter gpLetter : gpLetters) {
                        if (gpLetter.getGpName().equals(patientview.getGpdetails().getGpname())) {
                            gpNameExists = true;
                        }
                    }

                    if (!gpNameExists) {
                        LOG.info("gpLetters(0) is not claimed, no entry exists, create new letter");
                        // no entry for this specific GP name, create new entry
                        gpLetterService.add(patientview, fhirLink.getGroup());
                    }
                }
            } else {
                LOG.info("gpLetters is empty, create new letter");

                // GP details do not match any in GP letter table, create new entry
                gpLetterService.add(patientview, fhirLink.getGroup());
            }
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
                createGpLetter(fhirLink, patientview);
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
    public void validate(Patientview patientview) throws ImportResourceException {

        // Patient exists with this identifier
        try {
            patientService.matchPatientByIdentifierValue(patientview);
        } catch (ResourceNotFoundException rnf) {
            String errorMessage =  "Patient with identifier '"
                    + patientview.getPatient().getPersonaldetails().getNhsno() + "' does not exist in PatientView";
            LOG.error(errorMessage);
            throw new ImportResourceException(errorMessage);
        }

        // Group exists
        if (!organizationService.groupWithCodeExists(patientview.getCentredetails().getCentrecode())) {
            String errorMessage = "Group with code '" + patientview.getCentredetails().getCentrecode()
                    + "' does not exist in PatientView";
            LOG.error(errorMessage);
            throw new ImportResourceException(errorMessage);
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
