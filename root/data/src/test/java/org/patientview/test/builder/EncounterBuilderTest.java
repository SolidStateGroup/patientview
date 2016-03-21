package org.patientview.test.builder;

import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Encounter;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.builder.EncounterBuilder;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.util.Util;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/03/2016
 */
public class EncounterBuilderTest {

    @Test
    public void testBuildNew() throws Exception {
        Date now = new DateTime(new Date()).withMillisOfSecond(0).toDate();
        UUID subjectId = UUID.randomUUID();

        FhirEncounter fhirEncounter = new FhirEncounter();
        fhirEncounter.setDate(now);
        fhirEncounter.setEncounterType(EncounterTypes.SURGERY.toString());
        fhirEncounter.setStatus("some status");

        // build
        EncounterBuilder encounterBuilder
                = new EncounterBuilder(null, fhirEncounter, Util.createResourceReference(subjectId));
        Encounter encounter = encounterBuilder.build();

        Assert.assertNotNull("The encounter should not be null", encounter);
        Assert.assertEquals("EncounterType incorrect",
                encounter.getIdentifier().get(0).getValueSimple(), fhirEncounter.getEncounterType());
        Assert.assertEquals("Status incorrect", encounter.getType().get(0).getTextSimple(), fhirEncounter.getStatus());

        Assert.assertNotNull("The period should not be null", encounter.getPeriod());
        DateAndTime date = encounter.getPeriod().getStartSimple();
        Date expected = new Date(new GregorianCalendar(date.getYear(), date.getMonth() - 1,
                date.getDay(), date.getHour(), date.getMinute(), date.getSecond()).getTimeInMillis());
        Assert.assertEquals("Start Date incorrect", expected.getTime(), fhirEncounter.getDate().getTime());
    }
}
