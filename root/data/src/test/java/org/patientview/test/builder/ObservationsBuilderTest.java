package org.patientview.test.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.ResourceReference;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.builder.ObservationsBuilder;
import org.patientview.test.BaseTest;
import org.patientview.util.Util;
import org.springframework.util.CollectionUtils;

/**
 * Created by james@solidstategroup.com
 * Created on 03/09/2014
 */
public class ObservationsBuilderTest extends BaseTest {

    /**
     * Test: To create the observations without error
     *
     * @throws Exception
     */
    @Test
    public void testObservationBuilder() throws Exception {
        Patientview patientview = Util.unmarshallPatientRecord(getTestFile());

        ObservationsBuilder observationsBuilder = new ObservationsBuilder(patientview, new ResourceReference());
        observationsBuilder.build();

        Assert.assertTrue("The observations are not empty",
                !CollectionUtils.isEmpty(observationsBuilder.getObservations()));
    }
}
