package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.controller.BaseController;
import org.patientview.api.service.PractitionerService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirContact;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/11/2014
 */
@Service
public class PractitionerServiceImpl extends BaseController<PractitionerServiceImpl> implements PractitionerService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    @Named("fhir")
    private DataSource dataSource;

    public UUID addPractitioner(FhirPractitioner fhirPractitioner) throws FhirResourceException {
        Practitioner practitioner = new Practitioner();
        HumanName humanName = new HumanName();
        humanName.addFamilySimple(fhirPractitioner.getName());
        Enumeration<HumanName.NameUse> nameUse = new Enumeration<>(HumanName.NameUse.usual);
        humanName.setUse(nameUse);
        practitioner.setName(humanName);

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

        for (FhirContact fhirContact : fhirPractitioner.getContacts()) {
            if (StringUtils.isNotEmpty(fhirContact.getValue())) {
                Contact contact = practitioner.addTelecom();
                contact.setSystem(new Enumeration<>(Contact.ContactSystem.valueOf(fhirContact.getSystem())));
                contact.setValueSimple(fhirContact.getValue());
                contact.setUse(new Enumeration<>(Contact.ContactUse.valueOf(fhirContact.getUse())));
            }
        }

        FhirDatabaseEntity entity
                = fhirResource.createEntity(practitioner, ResourceType.Practitioner.name(), "practitioner");
        return entity.getLogicalId();
    }

    public List<UUID> getPractitionerLogicalUuidsByName(final String name) throws FhirResourceException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id ");
        query.append("FROM practitioner ");
        query.append("WHERE content -> 'name' -> 'family' = '\"");
        query.append(name);
        query.append("\"' ");

        // execute and return UUIDs
        try {
            Connection connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());

            List<UUID> uuids = new ArrayList<>();

            while ((results.next())) {
                uuids.add(UUID.fromString(results.getString(1)));
            }

            connection.close();
            return uuids;
        } catch (SQLException e) {
            throw new FhirResourceException(e);
        }
    }
}
