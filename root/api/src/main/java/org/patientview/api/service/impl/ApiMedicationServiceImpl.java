package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.Medication;
import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.api.controller.BaseController;
import org.patientview.api.model.FhirMedicationStatement;
import org.patientview.api.model.GpMedicationStatus;
import org.patientview.api.service.ApiMedicationService;
import org.patientview.api.service.GpMedicationService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.GpMedicationGroupCodes;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.util.DataUtils;
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
public class ApiMedicationServiceImpl extends BaseController<ApiMedicationServiceImpl> implements ApiMedicationService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private UserRepository userRepository;

    @Inject
    private GpMedicationService gpMedicationService;

    @Override
    public List<FhirMedicationStatement> getByUserId(final Long userId)
            throws ResourceNotFoundException, FhirResourceException {
        return getByUserId(userId, null, null);
    }

    @Override
    public List<FhirMedicationStatement> getByUserId(final Long userId, String fromDate, String toDate)
            throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<FhirMedicationStatement> fhirMedications = new ArrayList<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {

            boolean retrieveMedication = false;

            if (fhirLink.getGroup().getCode().equals(GpMedicationGroupCodes.ECS.toString())) {
                GpMedicationStatus gpMedicationStatus = gpMedicationService.getGpMedicationStatus(userId);
                if (gpMedicationStatus.getOptInStatus()) {
                    retrieveMedication = true;
                }
            } else {
                retrieveMedication = true;
            }

            if (retrieveMedication) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  content::varchar ");
                query.append("FROM    medicationstatement ");
                query.append("WHERE   content -> 'patient' ->> 'display' = '");
                query.append(fhirLink.getResourceId().toString());
                query.append("' ");

                if (fromDate != null && toDate != null) {
                    query.append("AND   content -> 'whenGiven' ->> 'start' >= '" + fromDate + "' ");
                    query.append("AND   content -> 'whenGiven' ->> 'end' <= '" + toDate + "' ");
                }
                query.append(" ORDER BY  content -> 'whenGiven' ->> 'start' DESC ");

                // get list of medication statements
                List<MedicationStatement> medicationStatements
                        = fhirResource.findResourceByQuery(query.toString(), MedicationStatement.class);

                // for each, create new transport object with medication found from resource reference
                for (MedicationStatement medicationStatement : medicationStatements) {

                    try {
                        JSONObject medicationJson = fhirResource.getResource(
                                UUID.fromString(medicationStatement.getMedication().getDisplaySimple()),
                                ResourceType.Medication);

                        Medication medication = (Medication) DataUtils.getResource(medicationJson);

                        org.patientview.persistence.model.FhirMedicationStatement fhirMedicationStatement =
                                new org.patientview.persistence.model.FhirMedicationStatement(
                                        medicationStatement, medication, fhirLink.getGroup());

                        fhirMedications.add(new FhirMedicationStatement(fhirMedicationStatement));

                    } catch (Exception e) {
                        throw new FhirResourceException(e.getMessage());
                    }
                }
            }
        }

        return fhirMedications;
    }
}
