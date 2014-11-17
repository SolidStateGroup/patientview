package org.patientview.importer.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.ResourceReference;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.importer.BaseTest;
import org.patientview.importer.Utility.Util;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/11/2014
 */
public class AllergyIntoleranceBuilderTest extends BaseTest {

    /**
     * Test: To create an AllergyIntolerance without error
     * @throws Exception
     */
    @Test
    public void testAllergyIntoleranceBuilder() throws Exception {
        Patientview patientview = Util.unmarshallPatientRecord(getTestFile());
        AllergyIntoleranceBuilder allergyIntoleranceBuilder
                = new AllergyIntoleranceBuilder(patientview.getPatient().getAllergy().get(0), new ResourceReference());
        AllergyIntolerance allergyIntolerance = allergyIntoleranceBuilder.build();

        Assert.assertNotNull("Created allergy intolerance should not be null", allergyIntolerance);
    }
}
