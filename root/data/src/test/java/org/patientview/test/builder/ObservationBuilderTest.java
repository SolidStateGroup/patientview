package org.patientview.test.builder;

import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Quantity;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.builder.ObservationBuilder;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.util.Util;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 21/03/2016
 */
public class ObservationBuilderTest {

    @Test
    public void testBuildNew() throws Exception {
        Date now = new DateTime(new Date()).withMillisOfSecond(0).toDate();
        UUID subjectId = UUID.randomUUID();

        FhirObservation fhirObservation = new FhirObservation();
        fhirObservation.setName("00");
        fhirObservation.setValue("<2");
        fhirObservation.setApplies(now);
        fhirObservation.setComments("some comment");

        // build
        ObservationBuilder observationBuilder
                = new ObservationBuilder(null, fhirObservation, Util.createResourceReference(subjectId));
        Observation observation = observationBuilder.build();

        Assert.assertNotNull("The observation should not be null", observation);
        Assert.assertEquals("Name incorrect", observation.getName().getTextSimple(), fhirObservation.getName());
        Assert.assertEquals("Comments incorrect", observation.getCommentsSimple(), fhirObservation.getComments());

        Assert.assertEquals("Value should be Quantity", observation.getValue().getClass(), Quantity.class);
        Quantity value = (Quantity) observation.getValue();
        Assert.assertNotNull("The value should not be null", value);
        Assert.assertEquals("Value incorrect", value.getValueSimple().toString(), "2.0");
        Assert.assertEquals("Comparator incorrect", value.getComparatorSimple(), Quantity.QuantityComparator.lessThan);

        org.hl7.fhir.instance.model.DateTime applies = (org.hl7.fhir.instance.model.DateTime) observation.getApplies();
        DateAndTime date = applies.getValue();
        Date expected = new Date(new GregorianCalendar(date.getYear(), date.getMonth() - 1,
                date.getDay(), date.getHour(), date.getMinute(), date.getSecond()).getTimeInMillis());
        Assert.assertEquals("Date incorrect", expected.getTime(), fhirObservation.getApplies().getTime());
    }
}
