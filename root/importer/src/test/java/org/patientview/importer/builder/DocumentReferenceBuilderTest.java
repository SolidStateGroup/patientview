package org.patientview.importer.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.ResourceReference;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.importer.BaseTest;
import org.patientview.importer.Utility.Util;
import org.springframework.util.CollectionUtils;

import java.util.List;

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
                = new DocumentReferenceBuilder(patientview, new ResourceReference());
        List<DocumentReference> documentReferences = documentReferenceBuilder.build();

        Assert.assertTrue("The documentReferences list should not be empty",
                !CollectionUtils.isEmpty(documentReferences));
    }
}
