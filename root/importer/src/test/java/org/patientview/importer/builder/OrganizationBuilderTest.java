package org.patientview.importer.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.Organization;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.importer.BaseTest;
import org.patientview.importer.Util.Util;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class OrganizationBuilderTest extends BaseTest {

    @Test
    public void testOrganizationBuilder() throws Exception {

        Patientview patientview = Util.unmarshallPatientRecord(getTestFile());
        OrganizationBuilder organizationBuilder = new OrganizationBuilder(patientview);
        Organization organization =  organizationBuilder.build();

        Assert.assertTrue("The organization should not be empty", organization != null);
    }
}
