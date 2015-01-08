package org.patientview.importer.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.Substance;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.importer.BaseTest;
import org.patientview.importer.Utility.Util;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/11/2014
 */
public class SubstanceBuilderTest extends BaseTest {

    /**
     * Test: To create a Substance without error
     * @throws Exception
     */
    @Test
    public void testSubstanceBuilder() throws Exception {
        Patientview patientview = Util.unmarshallPatientRecord(getTestFile());
        SubstanceBuilder substanceBuilder
                = new SubstanceBuilder(patientview.getPatient().getAllergy().get(0));
        Substance substance = substanceBuilder.build();

        Assert.assertNotNull("Created substance should not be null", substance);
    }
}
