package org.patientview.importer.manager.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.exception.ImportResourceException;
import org.patientview.importer.manager.ImportManager;
import org.patientview.importer.service.ConditionService;
import org.patientview.importer.service.EncounterService;
import org.patientview.importer.service.ObservationService;
import org.patientview.importer.service.OrganizationService;
import org.patientview.importer.service.PatientService;
import org.patientview.importer.service.PractitionerService;
import org.patientview.importer.service.impl.AbstractServiceImpl;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Identifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
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
    private Properties properties;


    @Override
    public boolean validate(Patientview patientview) {

        // patient exists
        try {
            Identifier identifier = patientService.matchPatientByIdentifierValue(patientview);
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
        ResourceReference patientReference;

        try {
            Date start = new Date();
            LOG.info("Starting to process data for NHS number: "
                    + patientview.getPatient().getPersonaldetails().getNhsno());

            // organization (Unit/centre details)
            UUID organizationUuid = organizationService.add(patientview);
            organizationReference = createResourceReference(organizationUuid);

            // practitioner (GP details)
            UUID practitionerUuid = practitionerService.add(patientview);
            practitionerReference = createResourceReference(practitionerUuid);

            // core patient object
            UUID patientUuid = patientService.add(patientview, practitionerReference);
            patientReference = createResourceReference(patientUuid);

            // observations (tests)
            observationService.add(patientview, patientReference);

            // conditions (diagnoses)
            conditionService.add(patientview, patientReference);

            // encounters (used for treatment and transplant status)
            encounterService.add(patientview, patientReference, organizationReference);

            Date end = new Date();
            LOG.info("Finished processing data for NHS number: "
                    + patientview.getPatient().getPersonaldetails().getNhsno()
                    + ". Took " + getDateDiff(start,end,TimeUnit.SECONDS) + " seconds.");

        } catch (FhirResourceException | ResourceNotFoundException e) {
            LOG.error("Unable to build patient {}", patientview.getPatient().getPersonaldetails().getNhsno());
            throw new ImportResourceException("Could not process patient data");
        }
    }

    public void removeOldData(Patientview patientview) throws ImportResourceException {
        int maxFhirLinkStored = Integer.parseInt(properties.getProperty("maximum.fhirlink.stored"));
        Date start = new Date();

        LOG.info("Removing old data, no more than " + maxFhirLinkStored + " FHIR records per group.");

        try {
            List<FhirLink> inactiveFhirlinks = patientService.getInactivePatientFhirLinksByGroup(patientview);

            if (inactiveFhirlinks.size() > maxFhirLinkStored -1) {

                // remove all old FHIR data and fhirlink leaving only maximum.fhirlink.stored remaining
                for(int i = inactiveFhirlinks.size() - 1;i > maxFhirLinkStored - 2; i--) {
                    FhirLink fhirLink = inactiveFhirlinks.get(i);

                    // remove FhirLink
                    patientService.deleteFhirLink(fhirLink);

                    // organization, practitioner are updated on import, do not need to remove old entries
                    patientService.deleteByResourceId(fhirLink.getResourceId());
                    observationService.deleteBySubjectId(fhirLink.getVersionId());
                    conditionService.deleteBySubjectId(fhirLink.getVersionId());
                    encounterService.deleteBySubjectId(fhirLink.getVersionId());
                }

                LOG.info("Finished removing old data for NHS number: "
                        + patientview.getPatient().getPersonaldetails().getNhsno()
                        + ". Took " + getDateDiff(start, new Date(), TimeUnit.SECONDS) + " seconds.");
            } else {
                LOG.info("No old data to remove.");
            }


        } catch (FhirResourceException | SQLException | ResourceNotFoundException e) {
            LOG.error("Unable to remove old data for patient {}",
                    patientview.getPatient().getPersonaldetails().getNhsno());
            throw new ImportResourceException("Could not remove old data");
        }
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    private ResourceReference createResourceReference(UUID uuid) {
        ResourceReference resourceReference = new ResourceReference();
        resourceReference.setDisplaySimple(uuid.toString());
        resourceReference.setReferenceSimple("uuid");
        return resourceReference;
    }
}
