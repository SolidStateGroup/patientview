package org.patientview.test.util;

import junit.framework.Assert;
import org.hl7.fhir.instance.model.Patient;
import org.junit.Test;
import org.patientview.test.BaseTest;
import org.patientview.util.Util;

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
        String content = Util.marshallFhirRecord(patient);
        Assert.assertTrue("The string should contain the nhs number", content != null && content.contains(nhsNumber));
    }
}
