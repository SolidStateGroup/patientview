package org.patientview.api.service.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.service.EncounterService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.PatientService;
import org.patientview.api.service.UktService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.UktException;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/01/2015
 */
@Service public class UktServiceImpl implements UktService {

    protected final Logger LOG = LoggerFactory.getLogger(UktService.class);

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private EncounterService encounterService;

    @Inject
    private GroupService groupService;

    @Inject
    private PatientService patientService;

    @Inject
    private Properties properties;

    @Inject
    private FhirResource fhirResource;

    @Override
    public void importData() throws ResourceNotFoundException, FhirResourceException, UktException {
        String importDirectory = properties.getProperty("ukt.import.directory");
        String importFilename = properties.getProperty("ukt.import.filename");
        Boolean importEnabled = Boolean.parseBoolean(properties.getProperty("ukt.import.enabled"));

        if (importEnabled) {
            BufferedReader br = null;

            try {
                br = new BufferedReader(new FileReader(importDirectory + "/" + importFilename));
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

                br.close();
            } catch (IOException e) {
                LOG.error("IOException, likely cannot read file: " + importDirectory + "/" + importFilename);
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException io) {
                        throw new UktException(io);
                    }
                }
                throw new UktException(e);
            }
        }
    }

    @Override
    public void exportData() throws ResourceNotFoundException, FhirResourceException, UktException {
        String exportDirectory = properties.getProperty("ukt.export.directory");
        String tempExportFilename = properties.getProperty("ukt.export.filename") + ".temp";
        String exportFilename = properties.getProperty("ukt.export.filename");
        Boolean exportEnabled = Boolean.parseBoolean(properties.getProperty("ukt.export.enabled"));

        if (exportEnabled) {
            try {
                BufferedWriter writer
                        = new BufferedWriter(new FileWriter(exportDirectory + "/" + tempExportFilename, false));
                List<User> patients = userRepository.findAllPatients();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                for (User user : patients) {
                    if (!CollectionUtils.isEmpty(user.getIdentifiers())) {
                        for (Identifier identifier : user.getIdentifiers()) {
                            writer.write("\"");
                            writer.write(identifier.getIdentifier());
                            writer.write("\",\"");
                            writer.write(user.getSurname());
                            writer.write("\",\"");
                            writer.write(user.getForename());
                            writer.write("\",\"");
                            if (user.getDateOfBirth() != null) {
                                writer.write(simpleDateFormat.format(user.getDateOfBirth()));
                            }
                            writer.write("\",\"");
                            writer.write(getPostcode(user));
                            writer.write("\"");
                            writer.newLine();
                        }
                    }
                }
                writer.flush();
                writer.close();

                File tempFile = new File(exportDirectory + "/" + tempExportFilename);
                File exportFile = new File(exportDirectory + "/" + exportFilename);
                exportFile.delete();
                FileUtils.copyFile(tempFile, exportFile);
                tempFile.delete();
            } catch (Exception e) {
                throw new UktException(e);
            }
        }
    }

    private String getPostcode(User user) throws FhirResourceException {
        try {
            if (user.getFhirLinks() != null) {
                for (FhirLink fhirLink : user.getFhirLinks()) {
                    Patient patient = patientService.get(fhirLink.getResourceId());
                    if (patient != null && !CollectionUtils.isEmpty(patient.getAddress())) {
                        if (StringUtils.isNotEmpty(patient.getAddress().get(0).getZipSimple())) {
                            return patient.getAddress().get(0).getZipSimple();
                        }
                    }
                }
            }
        } catch (Exception ignore) {
            // do nothing, could be due to JSON error, missing data etc
            return "";
        }

        return "";
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
