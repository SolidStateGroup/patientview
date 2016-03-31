package org.patientview.test.builder;

import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateAndTime;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.builder.ConditionBuilder;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.enums.DiagnosisSeverityTypes;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.util.Util;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/03/2016
 */
public class ConditionBuilderTest {

    @Test
    public void testBuildNew() throws Exception {
        Date now = new DateTime(new Date()).withMillisOfSecond(0).toDate();
        UUID subjectId = UUID.randomUUID();

        FhirCondition fhirCondition = new FhirCondition();
        fhirCondition.setCategory(DiagnosisTypes.DIAGNOSIS.toString());
        fhirCondition.setCode("00");
        fhirCondition.setNotes("notes");
        fhirCondition.setDate(now);
        fhirCondition.setSeverity(DiagnosisSeverityTypes.MAIN.toString());

        // build
        ConditionBuilder conditionBuilder
                = new ConditionBuilder(null, fhirCondition, Util.createResourceReference(subjectId));
        Condition condition = conditionBuilder.build();

        Assert.assertTrue("The condition should not be null", condition != null);
        Assert.assertEquals("Category incorrect", condition.getCategory().getTextSimple(), fhirCondition.getCategory());
        Assert.assertEquals("Code incorrect", condition.getCode().getTextSimple(), fhirCondition.getCode());
        Assert.assertEquals("Notes incorrect", condition.getNotesSimple(), fhirCondition.getNotes());
        Assert.assertEquals("Severity incorrect", condition.getSeverity().getTextSimple(), fhirCondition.getSeverity());

        DateAndTime date = condition.getDateAssertedSimple();
        Date expected = new Date(new GregorianCalendar(date.getYear(), date.getMonth() - 1,
                date.getDay(), date.getHour(), date.getMinute(), date.getSecond()).getTimeInMillis());
        Assert.assertEquals("Date incorrect", expected.getTime(), fhirCondition.getDate().getTime());
    }
}
