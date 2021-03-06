package org.patientview.test.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.Substance;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.builder.SubstanceBuilder;
import org.patientview.test.BaseTest;
import org.patientview.util.Util;

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
                = new SubstanceBuilder(patientview.getPatient().getAllergydetails().getAllergy().get(0));
        Substance substance = substanceBuilder.build();

        Assert.assertNotNull("Created substance should not be null", substance);
    }
}
