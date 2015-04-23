package org.patientview.importer.service.impl;

import generated.Patientview;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.config.utils.CommonUtils;
import org.patientview.importer.builder.PractitionerBuilder;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.importer.service.PractitionerService;
import org.patientview.config.exception.FhirResourceException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
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
    private BasicDataSource dataSource;

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
                        CommonUtils.cleanSql(data.getGpdetails().getGptelephone()));

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

    private List<Map<String, UUID>> getUuids(String familyName, String address1, String address2, String address3, 
                                             String address4, String postcode, String telephone) 
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

        // execute and return UUIDs
        try {
            Connection connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());

            List<Map<String, UUID>> uuids = new ArrayList<>();

            while ((results.next())) {
                Map<String, UUID> ids = new HashMap<>();
                ids.put("versionId", UUID.fromString(results.getString(1)));
                ids.put("logicalId", UUID.fromString(results.getString(2)));
                uuids.add(ids);
            }

            connection.close();
            return uuids;
        } catch (SQLException e) {
            throw new FhirResourceException(e);
        }
    }
}
