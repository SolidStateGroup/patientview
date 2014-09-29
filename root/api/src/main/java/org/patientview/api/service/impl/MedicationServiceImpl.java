package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.Medication;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.controller.BaseController;
import org.patientview.api.model.FhirMedicationStatement;
import org.patientview.api.service.MedicationService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/09/2014
 */
@Service
public class MedicationServiceImpl extends BaseController<MedicationServiceImpl> implements MedicationService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private UserRepository userRepository;

    private static final Logger LOG = LoggerFactory.getLogger(MedicationServiceImpl.class);

    @Override
    public List<FhirMedicationStatement> getByUserId(final Long userId)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<FhirMedicationStatement> fhirMedications = new ArrayList<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  content::varchar ");
                query.append("FROM    medicationstatement ");
                query.append("WHERE   content->> 'subject' = '{\"display\": \"");
                query.append(fhirLink.getVersionId().toString());
                query.append("\", \"reference\": \"uuid\"}'");

                // get list of medication statements
                List<MedicationStatement> medicationStatements
                        = fhirResource.findResourceByQuery(query.toString(), MedicationStatement.class);

                // for each, create new transport object with medication found from resource reference
                for (MedicationStatement medicationStatement : medicationStatements) {
                    Medication medication = (Medication) fhirResource.getResourceConverted(
                            UUID.fromString(medicationStatement.getMedication().getReferenceSimple())
                            , ResourceType.Medication);

                    fhirMedications.add(new FhirMedicationStatement(medicationStatement, medication));
                }
            }
        }

        return fhirMedications;
    }
}
