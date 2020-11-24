package org.patientview.service.impl;

import generated.Patientview;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.config.utils.CommonUtils;
import org.patientview.builder.PractitionerBuilder;
import org.patientview.persistence.model.FhirContact;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.enums.PractitionerRoles;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.PractitionerService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.util.DataUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
@Service
public class PractitionerServiceImpl extends AbstractServiceImpl<PractitionerService> implements PractitionerService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    @Named("fhir")
    private DataSource dataSource;

    private String nhsno;

    /**
     * Creates FHIR practitioner record from the Patientview object.
     * @param data Generated object from XML containing data to import
     */
    @Override
    public UUID add(final Patientview data) throws FhirResourceException {

        this.nhsno = data.getPatient().getPersonaldetails().getNhsno();
        //LOG.info(nhsno + ": Starting Practitioner Process");

        if (data.getGpdetails() != null) {
            try {

                // build FHIR object, accounting for blank gp name (replace with address if present)
                if (StringUtils.isEmpty(data.getGpdetails().getGpname())
                        && StringUtils.isEmpty(data.getGpdetails().getGpaddress1())) {
                    LOG.info(nhsno + ": Empty GP details, not adding");
                    return null;
                }

                if (StringUtils.isEmpty(data.getGpdetails().getGpname())) {
                    data.getGpdetails().setGpname(data.getGpdetails().getGpaddress1());
                    LOG.info(nhsno + ": Empty GP name, replacing with GP address 1");
                }

                PractitionerBuilder practitionerBuilder = new PractitionerBuilder(data);
                Practitioner importPractitioner = practitionerBuilder.build();

                List<Map<String, UUID>> uuids
                        = getUuids(CommonUtils.cleanSql(data.getGpdetails().getGpname()),
                        CommonUtils.cleanSql(data.getGpdetails().getGpaddress1()),
                        CommonUtils.cleanSql(data.getGpdetails().getGpaddress2()),
                        CommonUtils.cleanSql(data.getGpdetails().getGpaddress3()),
                        CommonUtils.cleanSql(data.getGpdetails().getGpaddress4()),
                        CommonUtils.cleanSql(data.getGpdetails().getGppostcode()),
                        CommonUtils.cleanSql(data.getGpdetails().getGptelephone()),
                        null);

                if (!uuids.isEmpty()) {
                    // native update existing FHIR entities (should be a single row), return reference
                    UUID logicalId = null;

                    for (Map<String, UUID> objectData : uuids) {
                        fhirResource.updateEntity(importPractitioner,
                                ResourceType.Practitioner.name(), "practitioner", objectData.get("logicalId"));
                        logicalId = objectData.get("logicalId");
                    }

                    LOG.info(nhsno + ": Existing Practitioner, " + logicalId);
                    return logicalId;

                } else {
                    // native create new FHIR object
                    FhirDatabaseEntity entity = fhirResource.createEntity(
                            importPractitioner, ResourceType.Practitioner.name(), "practitioner");
                    LOG.info(nhsno + ": New Practitioner, " + entity.getLogicalId());
                    return entity.getLogicalId();
                }
            } catch (FhirResourceException e) {
                LOG.error(nhsno + ": Unable to build practitioner");
                throw e;
            }
        } else {
            return null;
        }
    }

    @Override
    public UUID add(FhirPractitioner fhirPractitioner) throws FhirResourceException {
        Practitioner practitioner = new Practitioner();
        HumanName humanName = new HumanName();
        humanName.addFamilySimple(fhirPractitioner.getName());
        Enumeration<HumanName.NameUse> nameUse = new Enumeration<>(HumanName.NameUse.usual);
        humanName.setUse(nameUse);
        practitioner.setName(humanName);

        if (StringUtils.isNotEmpty(fhirPractitioner.getAddress1())
                || StringUtils.isNotEmpty(fhirPractitioner.getAddress2())
                || StringUtils.isNotEmpty(fhirPractitioner.getAddress3())
                || StringUtils.isNotEmpty(fhirPractitioner.getAddress4())
                || StringUtils.isNotEmpty(fhirPractitioner.getPostcode())) {
            Address address = new Address();
            if (StringUtils.isNotEmpty(fhirPractitioner.getAddress1())) {
                address.addLineSimple(fhirPractitioner.getAddress1());
            }
            if (StringUtils.isNotEmpty(fhirPractitioner.getAddress2())) {
                address.setCitySimple(fhirPractitioner.getAddress2());
            }
            if (StringUtils.isNotEmpty(fhirPractitioner.getAddress3())) {
                address.setStateSimple(fhirPractitioner.getAddress3());
            }
            if (StringUtils.isNotEmpty(fhirPractitioner.getAddress4())) {
                address.setCountrySimple(fhirPractitioner.getAddress4());
            }
            if (StringUtils.isNotEmpty(fhirPractitioner.getPostcode())) {
                address.setZipSimple(fhirPractitioner.getPostcode());
            }
            practitioner.setAddress(address);
        }

        if (!CollectionUtils.isEmpty(fhirPractitioner.getContacts())) {
            for (FhirContact fhirContact : fhirPractitioner.getContacts()) {
                if (StringUtils.isNotEmpty(fhirContact.getValue())) {
                    Contact contact = practitioner.addTelecom();
                    contact.setSystem(new Enumeration<>(Contact.ContactSystem.valueOf(fhirContact.getSystem())));
                    contact.setValueSimple(fhirContact.getValue());
                    contact.setUse(new Enumeration<>(Contact.ContactUse.valueOf(fhirContact.getUse())));
                }
            }
        }

        if (StringUtils.isNotEmpty(fhirPractitioner.getRole())) {
            practitioner.addRole().setTextSimple(fhirPractitioner.getRole());
        }

        FhirDatabaseEntity entity
                = fhirResource.createEntity(practitioner, ResourceType.Practitioner.name(), "practitioner");
        return entity.getLogicalId();
    }

    @Override
    public void addOtherPractitionersToPatient(Patientview data, FhirLink fhirLink) throws FhirResourceException {
        if (data.getPatient().getClinicaldetails() != null) {
            List<UUID> practitionerUuidsToAdd = new ArrayList<>();

            // named consultant
            if (StringUtils.isNotEmpty(data.getPatient().getClinicaldetails().getNamedconsultant())) {
                practitionerUuidsToAdd.add(storeOtherPractitioner(
                    data.getPatient().getClinicaldetails().getNamedconsultant(), PractitionerRoles.NAMED_CONSULTANT));
            }

            // IBD nurse
            if (StringUtils.isNotEmpty(data.getPatient().getClinicaldetails().getIbdnurse())) {
                practitionerUuidsToAdd.add(storeOtherPractitioner(
                    data.getPatient().getClinicaldetails().getIbdnurse(), PractitionerRoles.IBD_NURSE));
            }

            // add practitioners to existing patient
            if (!CollectionUtils.isEmpty(practitionerUuidsToAdd)) {
                try {
                    Patient patient = (Patient) DataUtils.getResource(
                            fhirResource.getResource(fhirLink.getResourceId(), ResourceType.Patient));

                    for (UUID practitionerUuid : practitionerUuidsToAdd) {
                        ResourceReference careProvider = patient.addCareProvider();
                        careProvider.setReferenceSimple("uuid");
                        careProvider.setDisplaySimple(practitionerUuid.toString());
                    }

                    fhirResource.updateEntity(
                            patient, ResourceType.Patient.name(), "patient", fhirLink.getResourceId());
                } catch (Exception e) {
                    LOG.error(nhsno + ": Unable to add other practitioners to patient, continuing..");
                }
            }
        }
    }

    private List<Map<String, UUID>> getUuids(String familyName, String address1, String address2, String address3,
                                             String address4, String postcode, String telephone, String role)
            throws FhirResourceException {

        // build query, handle db stored '' for ' in text e.g. O''DONNEL
        StringBuilder query = new StringBuilder();
        query.append("SELECT  version_id, logical_id ");
        query.append("FROM practitioner ");
        query.append("WHERE content -> 'name' #>> '{family,0}' = '");
        query.append(familyName.replace("'","''"));
        query.append("' ");

        if (StringUtils.isNotEmpty(address1)) {
            query.append("AND content -> 'address' #>> '{line,0}' = '");
            query.append(address1.replace("'","''"));
            query.append("' ");
        } else {
            query.append("AND (content -> 'address' #>> '{line,0}') IS NULL ");
        }

        if (StringUtils.isNotEmpty(address2)) {
            query.append("AND content -> 'address' ->> 'city' = '");
            query.append(address2.replace("'","''"));
            query.append("' ");
        } else {
            query.append("AND (content -> 'address' ->> 'city') IS NULL ");
        }

        if (StringUtils.isNotEmpty(address3)) {
            query.append("AND content -> 'address' ->> 'state' = '");
            query.append(address3.replace("'","''"));
            query.append("' ");
        } else {
            query.append("AND (content -> 'address' ->> 'state') IS NULL ");
        }

        if (StringUtils.isNotEmpty(address4)) {
            query.append("AND content -> 'address' ->> 'country' = '");
            query.append(address4.replace("'","''"));
            query.append("' ");
        } else {
            query.append("AND (content -> 'address' ->> 'country') IS NULL ");
        }

        if (StringUtils.isNotEmpty(postcode)) {
            query.append("AND content -> 'address' ->> 'zip' = '");
            query.append(postcode.replace("'","''"));
            query.append("' ");
        } else {
            query.append("AND (content -> 'address' ->> 'zip') IS NULL ");
        }

        if (StringUtils.isNotEmpty(telephone)) {
            query.append("AND content #> '{telecom,0}' ->> 'value' = '");
            query.append(telephone.replace("'","''"));
            query.append("' ");
        } else {
            query.append("AND (content #> '{telecom,0}' ->> 'value') IS NULL ");
        }

        if (StringUtils.isNotEmpty(role)) {
            query.append("AND content #> '{role,0}' ->> 'text' = '");
            query.append(role.replace("'","''"));
            query.append("' ");
        } else {
            query.append("AND (content #> '{role,0}' ->> 'text') IS NULL ");
        }

        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;

        // execute and return UUIDs
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());

            List<Map<String, UUID>> uuids = new ArrayList<>();

            while ((results.next())) {
                Map<String, UUID> ids = new HashMap<>();
                ids.put("versionId", UUID.fromString(results.getString(1)));
                ids.put("logicalId", UUID.fromString(results.getString(2)));
                uuids.add(ids);
            }

            return uuids;
        } catch (SQLException e) {
            throw new FhirResourceException(e);
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }
    }

    private UUID storeOtherPractitioner(String name, PractitionerRoles role) throws FhirResourceException {
        // build simple practitioner, setting role
        Practitioner practitioner = new Practitioner();

        HumanName humanName = new HumanName();
        humanName.addFamilySimple(CommonUtils.cleanSql(name));
        Enumeration<HumanName.NameUse> nameUse = new Enumeration<>(HumanName.NameUse.usual);
        humanName.setUse(nameUse);
        practitioner.setName(humanName);

        practitioner.addRole().setTextSimple(role.toString());

        // check existing by role
        List<Map<String, UUID>> uuids = getUuids(CommonUtils.cleanSql(name),
                null, null, null, null, null, null, role.toString());

        if (!uuids.isEmpty()) {
            // native update existing FHIR entities (should be a single row), return reference
            UUID logicalId = null;

            for (Map<String, UUID> objectData : uuids) {
                fhirResource.updateEntity(practitioner,
                        ResourceType.Practitioner.name(), "practitioner", objectData.get("logicalId"));
                logicalId = objectData.get("logicalId");
            }

            LOG.info(nhsno + ": Existing " + role.toString() + ", " + logicalId);
            return logicalId;
        } else {
            // native create new FHIR object
            FhirDatabaseEntity entity = fhirResource.createEntity(
                    practitioner, ResourceType.Practitioner.name(), "practitioner");
            LOG.info(nhsno + ": New " + role.toString() + ", " + entity.getLogicalId());
            return entity.getLogicalId();
        }
    }

    @Override
    public List<UUID> getPractitionerLogicalUuidsByName(final String name) throws FhirResourceException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id ");
        query.append("FROM practitioner ");
        query.append("WHERE content -> 'name' -> 'family' = '[\"");
        query.append(name);
        query.append("\"]'");

        // execute and return UUIDs
        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());

            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

            return uuids;
        } catch (SQLException e) {
            throw new FhirResourceException(e);
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }
    }

    @Override
    public List<UUID> getPractitionerLogicalUuidsByNameAndRole(final String name, final String role)
            throws FhirResourceException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id ");
        query.append("FROM practitioner ");
        query.append("WHERE content -> 'name' -> 'family' = '[\"");
        query.append(name.replace("'","''"));
        query.append("\"]' AND content ->> 'role' = '[{\"text\": \"");
        query.append(role);
        query.append("\"}]'");

        // execute and return UUIDs
        Connection connection = null;
        java.sql.Statement statement = null;
        ResultSet results = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            results = statement.executeQuery(query.toString());

            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

            return uuids;
        } catch (SQLException e) {
            throw new FhirResourceException(e);
        } finally {
            DbUtils.closeQuietly(connection, statement, results);
        }
    }
}
