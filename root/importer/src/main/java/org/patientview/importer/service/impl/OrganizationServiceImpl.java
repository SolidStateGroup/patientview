package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.Organization;
import org.json.JSONObject;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.builder.OrganizationBuilder;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.service.OrganizationService;
import org.patientview.importer.util.Util;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.repository.GroupRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Service
public class OrganizationServiceImpl extends AbstractServiceImpl<OrganizationService> implements OrganizationService{

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GroupRepository groupRepository;

    /**
     * Creates FHIR organization (unit/centre) record from the Patientview object.
     *
     * @param data
     */
    @Override
    public UUID add(final Patientview data) throws ResourceNotFoundException {

        // validate that group exists in patientview using persistence module, otherwise throw exception
        if (groupRepository.findByCode(data.getCentredetails().getCentrecode()) == null) {
            LOG.error("Unable to build organization, group not found");
            throw new ResourceNotFoundException("Unable to build organization, group not found");
        }

        OrganizationBuilder organizationBuilder = new OrganizationBuilder(data);

        try {
            JSONObject jsonObject = create(organizationBuilder.build());
            LOG.info("Processed Organization");
            return Util.getResourceId(jsonObject);
        } catch (FhirResourceException e) {
            LOG.error("Unable to build organization");
            return null;
        }
    }

    private JSONObject create(Organization organization) throws FhirResourceException {
        try {
            return fhirResource.create(organization);
        } catch (Exception e) {
            LOG.error("Could not build organization resource", e);
            throw new FhirResourceException(e.getMessage());
        }
    }
}


