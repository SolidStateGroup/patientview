package org.patientview.importer.service.impl;

import generated.Patientview;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.config.utils.CommonUtils;
import org.patientview.importer.builder.PractitionerBuilder;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.importer.service.PractitionerService;
import org.patientview.importer.Utility.Util;
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
     *
     * @param data
     */
    @Override
    public UUID add(final Patientview data) throws FhirResourceException {

        this.nhsno = data.getPatient().getPersonaldetails().getNhsno();
        LOG.info(nhsno + ": Starting Practitioner Process");

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
                        = getUuidsByFamilyName(CommonUtils.cleanSql(data.getGpdetails().getGpname()));

                if (!uuids.isEmpty()) {
                    // update existing FHIR entities (should be a single row), return reference
                    UUID updatedResourceId = null;

                    for (Map<String, UUID> objectData : uuids) {
                        try {
                            updatedResourceId = objectData.get("logicalId");
                            fhirResource.updateFhirObject(
                                    importPractitioner, objectData.get("logicalId"), objectData.get("versionId"));
                        } catch (FhirResourceException e) {
                            LOG.error(nhsno + ": Could not update practitioner, " + e.getMessage(), e);
                        }
                    }

                    LOG.info(nhsno + ": Existing Practitioner, " + updatedResourceId);
                    return updatedResourceId;

                } else {
                    // create new FHIR object
                    JSONObject jsonObject = create(importPractitioner);
                    LOG.info(nhsno + ": Processed new Practitioner");
                    return Util.getResourceId(jsonObject);
                }

            } catch (FhirResourceException e) {
                LOG.error(nhsno + ": Unable to build practitioner");
                throw e;
            }
        } else {
            return null;
        }
    }

    private List<Map<String, UUID>> getUuidsByFamilyName(String code) throws FhirResourceException {

        // handle db stored '' for ' in names e.g. O''DONNEL
        code = code.replace("'","''");

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT  version_id, logical_id ");
        query.append("FROM practitioner ");
        query.append("WHERE content -> 'name' #>> '{family,0}' = '");
        query.append(code);
        query.append("' ");

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

    private JSONObject create(Practitioner practitioner) throws FhirResourceException {
        try {
            return fhirResource.create(practitioner);
        } catch (Exception e) {
            LOG.error(nhsno + ": Could not build practitioner resource", e);
            throw new FhirResourceException(e.getMessage());
        }
    }
}


