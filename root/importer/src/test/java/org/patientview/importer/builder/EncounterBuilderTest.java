package org.patientview.importer.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.ResourceReference;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.importer.BaseTest;
import org.patientview.importer.util.Util;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class EncounterBuilderTest extends BaseTest {

    @Test
    public void testEncounterBuilder() throws Exception {
        Patientview patientview = Util.unmarshallPatientRecord(getTestFile());
        EncountersBuilder encountersBuilder = new EncountersBuilder(patientview,
                new ResourceReference(), new ResourceReference());
        List<Encounter> encounters = encountersBuilder.build();

        Assert.assertTrue("The conditions list should not be empty", !CollectionUtils.isEmpty(encounters));
    }
}
