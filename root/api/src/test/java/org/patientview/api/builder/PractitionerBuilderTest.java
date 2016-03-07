package org.patientview.api.builder;

import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Practitioner;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.persistence.model.FhirContact;
import org.patientview.persistence.model.FhirPractitioner;

import java.util.ArrayList;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/03/2016
 */
public class PractitionerBuilderTest {

    @Test
    public void testBuildNew() throws Exception {        
        // FhirPractitioner, from importer
        FhirPractitioner fhirPractitioner = new FhirPractitioner();
        fhirPractitioner.setName("name");
        fhirPractitioner.setAddress1("address1");
        fhirPractitioner.setAddress2("address2");
        fhirPractitioner.setAddress3("address3");
        fhirPractitioner.setAddress4("address4");
        fhirPractitioner.setPostcode("postcode");
        fhirPractitioner.setContacts(new ArrayList<FhirContact>());
        fhirPractitioner.getContacts().add(
                new FhirContact(Contact.ContactSystem.phone.toCode(), Contact.ContactUse.work.toCode(), "1234"));

        // build patient (no practitioner)
        PractitionerBuilder practitionerBuilder = new PractitionerBuilder(null, fhirPractitioner);
        Practitioner practitioner = practitionerBuilder.build();

        Assert.assertNotNull("The practitioner should not be null", practitioner);
        Assert.assertEquals("Name incorrect", 
                practitioner.getName().getFamily().get(0).getValue(), fhirPractitioner.getName());
        Assert.assertEquals("Address1 incorrect", 
                practitioner.getAddress().getLine().get(0).getValue(), fhirPractitioner.getAddress1());
        Assert.assertEquals("Address2 incorrect", 
                practitioner.getAddress().getCitySimple(), fhirPractitioner.getAddress2());
        Assert.assertEquals("Address3 incorrect", 
                practitioner.getAddress().getStateSimple(), fhirPractitioner.getAddress3());
        Assert.assertEquals("Address4 incorrect", 
                practitioner.getAddress().getCountrySimple(), fhirPractitioner.getAddress4());
        Assert.assertEquals("Postcode incorrect", 
                practitioner.getAddress().getZipSimple(), fhirPractitioner.getPostcode());
        Assert.assertEquals("Contact incorrect", 
                practitioner.getTelecom().get(0).getValueSimple(),
                fhirPractitioner.getContacts().get(0).getValue());
    }

    @Test
    public void testBuildUpdateAddress() throws Exception {        
        // FhirPractitioner, from importer
        FhirPractitioner fhirPractitioner = new FhirPractitioner();
        fhirPractitioner.setName("name");
        fhirPractitioner.setAddress1("address1");
        fhirPractitioner.setAddress2("address2");
        fhirPractitioner.setAddress3("address3");
        fhirPractitioner.setAddress4("address4");
        fhirPractitioner.setPostcode("postcode");
        fhirPractitioner.setContacts(new ArrayList<FhirContact>());
        fhirPractitioner.getContacts().add(
                new FhirContact(Contact.ContactSystem.phone.toCode(), Contact.ContactUse.work.toCode(), "1234"));

        // build practitioner
        PractitionerBuilder practitionerBuilder = new PractitionerBuilder(null, fhirPractitioner);
        Practitioner practitioner = practitionerBuilder.build();

        FhirPractitioner fhirPractitionerUpdate = new FhirPractitioner();
        fhirPractitionerUpdate.setAddress1("address1update");
        fhirPractitionerUpdate.setAddress2("address2update");
        fhirPractitionerUpdate.setAddress3("address3update");
        fhirPractitionerUpdate.setAddress4("address4update");
        fhirPractitionerUpdate.setPostcode("postcodeupdate");
        
        practitionerBuilder = new PractitionerBuilder(practitioner, fhirPractitionerUpdate);
        Practitioner practitionerUpdate = practitionerBuilder.build();

        Assert.assertNotNull("The practitionerUpdate should not be null", practitionerUpdate);
        Assert.assertEquals("Name incorrect (should not change)",
                practitionerUpdate.getName().getFamily().get(0).getValue(), fhirPractitioner.getName());
        Assert.assertEquals("Address1 incorrect", 
                practitionerUpdate.getAddress().getLine().get(0).getValue(), fhirPractitionerUpdate.getAddress1());
        Assert.assertEquals("Address2 incorrect", 
                practitionerUpdate.getAddress().getCitySimple(), fhirPractitionerUpdate.getAddress2());
        Assert.assertEquals("Address3 incorrect", 
                practitionerUpdate.getAddress().getStateSimple(), fhirPractitionerUpdate.getAddress3());
        Assert.assertEquals("Address4 incorrect", 
                practitionerUpdate.getAddress().getCountrySimple(), fhirPractitionerUpdate.getAddress4());
        Assert.assertEquals("Postcode incorrect", 
                practitionerUpdate.getAddress().getZipSimple(), fhirPractitionerUpdate.getPostcode());
        Assert.assertEquals("Contact incorrect (should not change)",
                practitionerUpdate.getTelecom().get(0).getValueSimple(),
                fhirPractitioner.getContacts().get(0).getValue());
    }

    @Test
    public void testBuildUpdateWorkPhone() throws Exception {
        // FhirPractitioner, from importer
        FhirPractitioner fhirPractitioner = new FhirPractitioner();
        fhirPractitioner.setName("name");
        fhirPractitioner.setAddress1("address1");
        fhirPractitioner.setAddress2("address2");
        fhirPractitioner.setAddress3("address3");
        fhirPractitioner.setAddress4("address4");
        fhirPractitioner.setPostcode("postcode");
        fhirPractitioner.setContacts(new ArrayList<FhirContact>());
        fhirPractitioner.getContacts().add(
                new FhirContact(Contact.ContactSystem.phone.toCode(), Contact.ContactUse.work.toCode(), "1234"));

        // build practitioner
        PractitionerBuilder practitionerBuilder = new PractitionerBuilder(null, fhirPractitioner);
        Practitioner practitioner = practitionerBuilder.build();

        FhirPractitioner fhirPractitionerUpdate = new FhirPractitioner();
        fhirPractitionerUpdate.setContacts(new ArrayList<FhirContact>());
        fhirPractitionerUpdate.getContacts().add(
                new FhirContact(Contact.ContactSystem.phone.toCode(), Contact.ContactUse.work.toCode(), "5678"));

        practitionerBuilder = new PractitionerBuilder(practitioner, fhirPractitionerUpdate);
        Practitioner practitionerUpdate = practitionerBuilder.build();

        Assert.assertNotNull("The practitionerUpdate should not be null", fhirPractitionerUpdate);
        Assert.assertEquals("Name incorrect (should not change)",
                practitionerUpdate.getName().getFamily().get(0).getValue(), fhirPractitioner.getName());
        Assert.assertEquals("Updated Contact incorrect",
                practitionerUpdate.getTelecom().get(0).getValueSimple(),
                fhirPractitionerUpdate.getContacts().get(0).getValue());
    }
}
