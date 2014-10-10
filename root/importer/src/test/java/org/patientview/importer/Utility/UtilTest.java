package org.patientview.importer.Utility;

import junit.framework.Assert;
import org.hl7.fhir.instance.model.Patient;
import org.junit.Test;
import org.patientview.importer.BaseTest;
import org.patientview.persistence.exception.FhirResourceException;

public class UtilTest extends BaseTest {

    public void setUp() throws Exception {
        super.setUp();

    }

    /**
     * Test: See if the object can be marshalled into JSON
     * Fail: Exception raised
     * @throws Exception
     */
    @Test
    public void testMarshallFhirRecord() throws Exception {
        String nhsNumber = "675765765";
        Patient patient = FhirTestUtil.createTestPatient(nhsNumber);
        String content = null;
        try {
            content = org.patientview.importer.Utility.Util.marshallFhirRecord(patient);
        } catch (FhirResourceException e) {
            e.printStackTrace();
            org.junit.Assert.fail("An exception should not be raised");        }

        Assert.assertTrue("The string should contain the nhs number", content != null && content.contains(nhsNumber));
    }
}