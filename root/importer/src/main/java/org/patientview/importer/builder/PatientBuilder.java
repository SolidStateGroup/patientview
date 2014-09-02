package org.patientview.importer.builder;

import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Patient;

/**
 * This is going to mapping between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by james@solidstategroup.com
 * Created on 26/08/2014
 */
public class PatientBuilder {

    private Patientview oldPatient;

    public PatientBuilder(Patientview oldPatient) {
        this.oldPatient = oldPatient;
    }

    public Patient build() {

        Patient newPatient = new Patient();
        createHumanName(newPatient, oldPatient);
        createAddress(newPatient, oldPatient);
        createContactDetails(newPatient, oldPatient);
        return newPatient;
    }

    private Patient createHumanName(Patient newPatient, Patientview oldPatient) {
        HumanName humanName = newPatient.addName();

        humanName.addFamilySimple(oldPatient.getPatient().getPersonaldetails().getSurname());
        humanName.addGivenSimple(oldPatient.getPatient().getPersonaldetails().getForename());

        Enumeration<HumanName.NameUse> nameUse = new Enumeration(HumanName.NameUse.usual);
        humanName.setUse(nameUse);
        return newPatient;
    }

    private Patient createAddress(Patient newPatient, Patientview oldPatient) {
        Address address = newPatient.addAddress();
        address.addLineSimple(oldPatient.getPatient().getPersonaldetails().getAddress1());
        address.setCitySimple(oldPatient.getPatient().getPersonaldetails().getAddress2());
        address.setStateSimple(oldPatient.getPatient().getPersonaldetails().getAddress3());
        address.setCountrySimple(oldPatient.getPatient().getPersonaldetails().getAddress4());
        address.setZipSimple(oldPatient.getPatient().getPersonaldetails().getPostcode());

        return newPatient;
    }

    private Patient createContactDetails(Patient newPatient, Patientview oldPatient) {
        Patient.ContactComponent contactComponent = newPatient.addContact();

        if (StringUtils.isNotEmpty(oldPatient.getPatient().getPersonaldetails().getMobile())) {
            Contact contact = contactComponent.addTelecom();
            contact.setValueSimple(oldPatient.getPatient().getPersonaldetails().getMobile());
            contact.setSystem(new Enumeration(Contact.ContactSystem.phone));
        }

        if (StringUtils.isNotEmpty(oldPatient.getPatient().getPersonaldetails().getTelephone1())) {
            Contact contact = contactComponent.addTelecom();
            contact.setValueSimple(oldPatient.getPatient().getPersonaldetails().getTelephone1());
            contact.setSystem(new Enumeration(Contact.ContactSystem.phone));
        }

        if (StringUtils.isNotEmpty(oldPatient.getPatient().getPersonaldetails().getTelephone2())) {
            Contact contact = contactComponent.addTelecom();
            contact.setValueSimple(oldPatient.getPatient().getPersonaldetails().getTelephone2());
            contact.setSystem(new Enumeration(Contact.ContactSystem.phone));
        }
        return  newPatient;

    }

}
