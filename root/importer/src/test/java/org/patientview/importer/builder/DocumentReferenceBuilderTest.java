package org.patientview.importer.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.ResourceReference;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.importer.BaseTest;
import org.patientview.importer.Utility.Util;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/11/2014
 */
public class DocumentReferenceBuilderTest extends BaseTest {

    /**
     * Test: To create the documentReferences without error
     *
     * @throws Exception
     */
    @Test
    public void testLetterBuilder() throws Exception {
        Patientview patientview = Util.unmarshallPatientRecord(getTestFile());
        DocumentReferenceBuilder documentReferenceBuilder
                = new DocumentReferenceBuilder(patientview.getPatient().getLetterdetails().getLetter().get(0),
                new ResourceReference());
        DocumentReference documentReference = documentReferenceBuilder.build();

        Assert.assertNotNull("The documentReference should not be null", documentReference);
    }
}
