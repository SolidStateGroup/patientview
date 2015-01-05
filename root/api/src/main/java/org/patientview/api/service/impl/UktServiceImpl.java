package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.service.EncounterService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.UktService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.resource.FhirResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/01/2015
 */
@Service
public class UktServiceImpl implements UktService {

    protected final Logger LOG = LoggerFactory.getLogger(UktService.class);

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private EncounterService encounterService;

    @Inject
    private GroupService groupService;

    @Inject
    private Properties properties;

    @Inject
    private FhirResource fhirResource;

    @Override
    public void importData() throws ResourceNotFoundException, FhirResourceException {
        String importDirectory = properties.getProperty("ukt.import.directory");
        String importFilename = properties.getProperty("ukt.import.filename");

        try {
            BufferedReader br = new BufferedReader(new FileReader(importDirectory + "/" + importFilename));
            String line;

            while ((line = br.readLine()) != null) {
                String identifier = line.split(" ")[0].trim();
                String kidneyStatus = line.split(" ")[2].trim();

                List<Identifier> identifiers = identifierRepository.findByValue(identifier);
                if (!CollectionUtils.isEmpty(identifiers)) {
                    User user = identifiers.get(0).getUser();

                    deleteExistingUktData(user);
                    addKidneyTransplantStatus(user, kidneyStatus);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addKidneyTransplantStatus(User user, String status)
            throws FhirResourceException, ResourceNotFoundException {

        if (!CollectionUtils.isEmpty(user.getFhirLinks())) {
            for (FhirLink fhirLink : user.getFhirLinks()) {
                if (!Util.isInEnum(fhirLink.getGroup().getCode(), HiddenGroupCodes.class)) {

                    UUID organizationUuid;

                    List<UUID> organizationUuids
                            = groupService.getOrganizationLogicalUuidsByCode(fhirLink.getGroup().getCode());

                    if (CollectionUtils.isEmpty(organizationUuids)) {
                        organizationUuid = groupService.addOrganization(fhirLink.getGroup());
                    } else {
                        organizationUuid = organizationUuids.get(0);
                    }

                    FhirEncounter fhirEncounter = new FhirEncounter();
                    fhirEncounter.setStatus(status);
                    fhirEncounter.setEncounterType(EncounterTypes.TRANSPLANT_STATUS_KIDNEY.toString());
                    encounterService.addEncounter(fhirEncounter, fhirLink, organizationUuid);
                }
            }
        }
    }

    private void deleteExistingUktData(User user) throws ResourceNotFoundException, FhirResourceException {
        List<UUID> encounterUuids
                = encounterService.getUuidsByUserIdAndType(user.getId(), EncounterTypes.TRANSPLANT_STATUS_KIDNEY);

        for (UUID uuid : encounterUuids) {
            fhirResource.delete(uuid, ResourceType.Encounter);
        }
    }
}
