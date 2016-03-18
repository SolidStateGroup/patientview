package org.patientview.test.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.ResourceReference;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.builder.ConditionsBuilder;
import org.patientview.test.BaseTest;
import org.patientview.util.Util;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
public class ConditionsBuilderTest extends BaseTest {

    /**
     * Test: To create the conditions without error
     *
     * @throws Exception
     */
    @Test
    public void testConditionBuilder() throws Exception {
        Patientview patientview = Util.unmarshallPatientRecord(getTestFile());
        ConditionsBuilder conditionsBuilder = new ConditionsBuilder(patientview, new ResourceReference());
        List<Condition> conditions = conditionsBuilder.build();

        Assert.assertTrue("The conditions list should not be empty", !CollectionUtils.isEmpty(conditions));
    }
}
