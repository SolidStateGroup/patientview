package org.patientview.importer.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceReference;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.importer.BaseTest;
import org.patientview.importer.Util.Util;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 03/09/2014
 */
public class ObservationBuilderTest extends BaseTest {


    /**
     * Test: To create the observations without error
     *
     * @throws Exception
     */
    @Test
    public void testObservationBuilder() throws Exception {

        Patientview patientview = Util.unmarshallPatientRecord(getTestFile());

        ObservationsBuilder observationsBuilder = new ObservationsBuilder(patientview ,
                new ResourceReference());
        List<Observation> observations = observationsBuilder.build();

        Assert.assertTrue("The observations are not empty", !CollectionUtils.isEmpty(observations));

    }


}
