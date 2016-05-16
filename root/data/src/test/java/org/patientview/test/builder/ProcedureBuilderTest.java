package org.patientview.test.builder;

import org.hl7.fhir.instance.model.Procedure;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.builder.ProcedureBuilder;
import org.patientview.persistence.model.FhirProcedure;
import org.patientview.util.Util;

import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 21/03/2016
 */
public class ProcedureBuilderTest {

    @Test
    public void testBuildNew() throws Exception {
        UUID subjectId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();

        FhirProcedure fhirProcedure = new FhirProcedure();
        fhirProcedure.setBodySite("hand");
        fhirProcedure.setType("IBD_SURGERYMAINPROCEDURE");

        // build
        ProcedureBuilder procedureBuilder = new ProcedureBuilder(
                null, fhirProcedure, Util.createResourceReference(subjectId),
                Util.createResourceReference(encounterId));
        Procedure procedure = procedureBuilder.build();

        Assert.assertNotNull("The procedure should not be null", procedure);
        Assert.assertEquals("Should have one body site", procedure.getBodySite().size(), 1);
        Assert.assertEquals("Body site incorrect", procedure.getBodySite().get(0).getTextSimple(),
                fhirProcedure.getBodySite());
        Assert.assertEquals("Type incorrect", procedure.getType().getTextSimple(), fhirProcedure.getType());
    }
}
