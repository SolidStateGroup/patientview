package org.patientview.api.builder;

import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Patient;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.persistence.model.FhirContact;
import org.patientview.persistence.model.FhirIdentifier;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.enums.IdentifierTypes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/03/2015
 */
public class PatientBuilderTest {

    @Test
    public void testBuildNew() throws Exception {

        Date now = new Date();
        
        // example FhirPatient POSTed to controller, when creating a new patient using API
        FhirPatient fhirPatient = new FhirPatient();
        fhirPatient.setForename("fore");
        fhirPatient.setSurname("sur");
        fhirPatient.setDateOfBirth(now);
        fhirPatient.setAddress1("address1");
        fhirPatient.setAddress2("address2");
        fhirPatient.setAddress3("address3");
        fhirPatient.setAddress4("address4");
        fhirPatient.setPostcode("postcode");
        fhirPatient.setIdentifiers(new ArrayList<FhirIdentifier>());
        fhirPatient.getIdentifiers().add(new FhirIdentifier(IdentifierTypes.NHS_NUMBER.toString(), "1111111111"));
        fhirPatient.setContacts(new ArrayList<FhirContact>());
        fhirPatient.getContacts().add(
                new FhirContact(Contact.ContactSystem.phone.toCode(), Contact.ContactUse.home.toCode(), "1234"));

        // build patient (no practitioner)
        PatientBuilder patientBuilder = new PatientBuilder(null, fhirPatient);
        Patient patient = patientBuilder.build();

        Assert.assertTrue("The patient is not empty", patient != null);
        Assert.assertEquals("Forename incorrect", 
                patient.getName().get(0).getGiven().get(0).getValue(), fhirPatient.getForename());
        Assert.assertEquals("Surname incorrect", 
                patient.getName().get(0).getFamily().get(0).getValue(), fhirPatient.getSurname());
        Assert.assertEquals("Address1 incorrect", 
                patient.getAddress().get(0).getLine().get(0).getValue(), fhirPatient.getAddress1());
        Assert.assertEquals("Address2 incorrect", 
                patient.getAddress().get(0).getCitySimple(), fhirPatient.getAddress2());
        Assert.assertEquals("Address3 incorrect", 
                patient.getAddress().get(0).getStateSimple(), fhirPatient.getAddress3());
        Assert.assertEquals("Address4 incorrect", 
                patient.getAddress().get(0).getCountrySimple(), fhirPatient.getAddress4());
        Assert.assertEquals("Postcode incorrect", 
                patient.getAddress().get(0).getZipSimple(), fhirPatient.getPostcode());
        Assert.assertEquals("Identifier incorrect", 
                patient.getIdentifier().get(0).getValueSimple(), fhirPatient.getIdentifiers().get(0).getValue());
        Assert.assertEquals("Contact incorrect", 
                patient.getContact().get(0).getTelecom().get(0).getValueSimple(), 
                fhirPatient.getContacts().get(0).getValue());

        // with date of birth not concerned with timezone
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date patientDate = df.parse(patient.getBirthDateSimple().toString());
        Date fhirPatientDate = df.parse(df.format(fhirPatient.getDateOfBirth()));
        Assert.assertEquals("Date of birth incorrect", patientDate, fhirPatientDate);
    }
}
