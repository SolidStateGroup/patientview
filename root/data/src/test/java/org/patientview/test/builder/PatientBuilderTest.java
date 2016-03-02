package org.patientview.test.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.Patient;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.builder.PatientBuilder;
import org.patientview.test.BaseTest;
import org.patientview.util.Util;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class PatientBuilderTest extends BaseTest {

    /**
     * Test: To create the patient without error
     *
     * @throws Exception
     */
    @Test
    public void testPatientBuilder() throws Exception {

        Patientview patientview = Util.unmarshallPatientRecord(getTestFile());

        // build patient (no practitioner)
        PatientBuilder patientBuilder = new PatientBuilder(patientview, null);
        Patient patient = patientBuilder.build();

        Assert.assertTrue("The patient is not empty", patient != null);
    }
}
