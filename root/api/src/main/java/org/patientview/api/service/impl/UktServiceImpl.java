package org.patientview.api.service.impl;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Patient;
import org.patientview.api.service.ApiPatientService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.UktService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.UktException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.service.EncounterService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/01/2015
 */
@Service
public class UktServiceImpl extends AbstractServiceImpl<UktServiceImpl> implements UktService {

    @Inject
    private ApiPatientService apiPatientService;

    @Inject
    private EncounterService encounterService;

    @Inject
    private GroupService groupService;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private Properties properties;

    @Inject
    private UserRepository userRepository;

    @Inject
    @Named("patientView")
    private BasicDataSource dataSource;

    /**
     * Store kidney transplant status for a User using a TRANSPLANT_STATUS_KIDNEY Encounter in FHIR.
     *
     * @param user   User to store a TRANSPLANT_STATUS_KIDNEY Encounter in FHIR for
     * @param status String status of kidney transplant
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    private void addKidneyTransplantStatus(User user, String status)
            throws FhirResourceException, ResourceNotFoundException {

        if (!CollectionUtils.isEmpty(user.getFhirLinks())) {
            for (FhirLink fhirLink : user.getFhirLinks()) {
                if (!ApiUtil.isInEnum(fhirLink.getGroup().getCode(), HiddenGroupCodes.class)) {

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
                    encounterService.add(fhirEncounter, fhirLink, organizationUuid);
                }
            }
        }
    }

    /**
     * Remove quotes ", brackets [ ] ( ) from input text String.
     *
     * @param text String to remove quotes ", brackets [ ] ( ) from
     * @return Cleaned String
     */
    private String clean(String text) {
        return text.replace("\"", " ")
                .replace("[", " ").replace("]", " ")
                .replace("(", " ").replace(")", " ")
                .replace("  ", " ");
    }

    /**
     * Export UKT data to text file from FHIR.
     *
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     * @throws UktException
     */
    @Override
    @Async
    public void exportData() throws ResourceNotFoundException, FhirResourceException, UktException {
        long start = System.currentTimeMillis();
        LOG.info("Starting exportData ...");

        String exportDirectory = properties.getProperty("ukt.export.directory");
        String tempExportFilename = properties.getProperty("ukt.export.filename") + ".temp";
        String logFilename = properties.getProperty("ukt.export.filename") + ".log";
        String exportFilename = properties.getProperty("ukt.export.filename");
        String tempFailExportFilename = properties.getProperty("ukt.export.filename") + ".failed.temp";
        String failExportFilename = properties.getProperty("ukt.export.filename") + ".failed.txt";
        Boolean exportEnabled = Boolean.parseBoolean(properties.getProperty("ukt.export.enabled"));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (exportEnabled) {

            Connection connection = null;
            try {
                BufferedWriter logWriter
                        = new BufferedWriter(new FileWriter(exportDirectory + "/" + logFilename, true));
                BufferedWriter writer
                        = new BufferedWriter(new FileWriter(exportDirectory + "/" + tempExportFilename, false));
                BufferedWriter writerFailed
                        = new BufferedWriter(new FileWriter(exportDirectory + "/" + tempFailExportFilename, false));

                logWriter.write(new Date().toString() + ": Started");
                logWriter.newLine();

                String querySql = "SELECT i.identifier, u.forename, u.surname, u.date_of_birth, " +
                        " (SELECT string_agg(cast(resource_id AS varchar), ', ')  " +
                        " FROM pv_fhir_link WHERE user_id = u.id) AS links " +
                        " FROM pv_user u " +
                        " LEFT JOIN pv_identifier i ON i.user_id = u.id " +
                        " JOIN pv_user_group_role ugr ON ugr.user_id = u.id " +
                        " JOIN  pv_role r ON r.id = ugr.role_id " +
                        " WHERE r.role_name = 'PATIENT' AND u.deleted = false" +
                        " GROUP BY u.id, i.identifier ";

                connection = dataSource.getConnection();
                java.sql.Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery(querySql);
                results.setFetchSize(1000);

                int totalPatients = 0;
                while ((results.next())) {
                    totalPatients++;
                    String identifier = results.getString(1);
                    String firstName = results.getString(2);
                    String lastName = results.getString(3);
                    Date dob = results.getDate(4);
                    String fhirlinks = results.getString(5);

                    if (isValidIdentifier(identifier)) {
                        //writer.write(getCsvRowFromUser(identifier, firstName, lastName, dob, fhirlinks));
                        writer.write("\"");
                        writer.write(identifier);
                        writer.write("\",\"");
                        writer.write(clean(lastName));
                        writer.write("\",\"");
                        writer.write(clean(firstName));
                        writer.write("\",\"");
                        if (dob != null) {
                            writer.write(simpleDateFormat.format(dob));
                        }
                        writer.write("\",\"");
                        writer.write(getPostcode(fhirlinks));
                        writer.write("\"");
                        writer.newLine();
                    } else {
                        writerFailed.write("\"");
                        writerFailed.write(StringUtils.isNotEmpty(identifier) ? identifier : "");
                        writerFailed.write("\",\"");
                        writerFailed.write(lastName);
                        writerFailed.write("\",\"");
                        writerFailed.write(firstName);
                        writerFailed.write("\",\"");
                        if (dob != null) {
                            writerFailed.write(simpleDateFormat.format(dob));
                        }
                        writerFailed.write("\",\"");
                        writerFailed.write(getPostcode(fhirlinks));
                        writerFailed.write("\"");
                        writerFailed.newLine();
                    }
                }

                connection.close();

                logWriter.write(new Date().toString() + ": " + totalPatients + " patients");
                logWriter.newLine();

                writer.flush();
                writer.close();
                writerFailed.flush();
                writerFailed.close();

                // copy temporary file
                File tempFile = new File(exportDirectory + "/" + tempExportFilename);
                File exportFile = new File(exportDirectory + "/" + exportFilename);
                exportFile.delete();
                FileUtils.copyFile(tempFile, exportFile);
                tempFile.delete();

                // copy temporary error file
                File tempFailFile = new File(exportDirectory + "/" + tempFailExportFilename);
                File exportFailFile = new File(exportDirectory + "/" + failExportFilename);
                exportFailFile.delete();
                FileUtils.copyFile(tempFailFile, exportFailFile);
                tempFailFile.delete();

                logWriter.write(new Date().toString() + ": Finished");
                logWriter.newLine();
                logWriter.flush();
                logWriter.close();
            } catch (Exception e) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e2) {
                        throw new UktException(e2);
                    }
                }
                throw new UktException(e);
            }
        }

        long stop = System.currentTimeMillis();
        LOG.info("TIMING exportData took {} milliseconds", (stop - start));
    }

    /**
     * Given a User, search FHIR for a postcode associated with any of the User's patient records.
     *
     * @param fhirLinks comma separated list of fhir links
     * @return String postcode, retrieved from FHIR
     * @throws FhirResourceException
     */
    private String getPostcode(String fhirLinks) throws FhirResourceException {
        try {
            if (StringUtils.isNotEmpty(fhirLinks)) {
                for (String fhirLink : fhirLinks.split(",")) {
                    Patient patient = apiPatientService.get(UUID.fromString(fhirLink));
                    if (patient != null && !CollectionUtils.isEmpty(patient.getAddress())) {
                        if (StringUtils.isNotEmpty(patient.getAddress().get(0).getZipSimple())) {
                            return clean(patient.getAddress().get(0).getZipSimple());
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

    /**
     * Import UKT transplant status data from text file and store in FHIR.
     *
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     * @throws UktException
     */
    @Override
    @Async
    public void importData() throws ResourceNotFoundException, FhirResourceException, UktException {
        String importDirectory = properties.getProperty("ukt.import.directory");
        String importFilename = properties.getProperty("ukt.import.filename");
        String logFilename = properties.getProperty("ukt.import.filename") + ".log";
        Boolean importEnabled = Boolean.parseBoolean(properties.getProperty("ukt.import.enabled"));

        if (importEnabled) {
            BufferedReader br = null;
            int count = 0;
            int updated = 0;

            try {
                BufferedWriter logWriter
                        = new BufferedWriter(new FileWriter(importDirectory + "/" + logFilename, true));
                br = new BufferedReader(new FileReader(importDirectory + "/" + importFilename));
                String line;
                logWriter.write(new Date().toString() + ": Started");
                logWriter.newLine();

                while ((line = br.readLine()) != null) {
                    String identifier = line.split(" ")[0].trim();
                    String kidneyStatus = line.split(" ")[2].trim();

                    List<Identifier> identifiers = identifierRepository.findByValue(identifier);
                    if (!CollectionUtils.isEmpty(identifiers)) {
                        User user = identifiers.get(0).getUser();
                        encounterService.deleteByUserAndType(user, EncounterTypes.TRANSPLANT_STATUS_KIDNEY);
                        addKidneyTransplantStatus(user, kidneyStatus);
                        updated++;
                    }
                    count++;
                }

                logWriter.write(new Date().toString() + ": Finished, " + count + " entries, "
                        + updated + " patients updated");
                logWriter.newLine();
                logWriter.flush();
                logWriter.close();
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

    /**
     * Check Identifier text value is a correct NHS_NUMBER, CHI_NUMBER or HSC_NUMBER.
     *
     * @param identifier Identifier to check has correct NHS_NUMBER, CHI_NUMBER or HSC_NUMBER
     * @return True if valid, false if not
     */
    private boolean isValidIdentifier(String identifier) {
        IdentifierTypes type = CommonUtils.getIdentifierType(identifier);
        return (type.equals(IdentifierTypes.NHS_NUMBER)
                || type.equals(IdentifierTypes.CHI_NUMBER)
                || type.equals(IdentifierTypes.HSC_NUMBER));
    }
}
