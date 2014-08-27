package org.patientview.importer.procedure;

import org.junit.Test;
import org.patientview.importer.BaseTest;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.util.FhirTestUtil;

public class FhirResourceIntegrationTest extends BaseTest {


    public void setUp() throws Exception {
        super.setUp();

    }

    @Test
    public void testCreateResource() throws Exception {
        FhirResource fhirResource = new FhirResource();
        System.out.println(fhirResource.createResource(FhirTestUtil.createTestPatient("1231321312")));
    }
}