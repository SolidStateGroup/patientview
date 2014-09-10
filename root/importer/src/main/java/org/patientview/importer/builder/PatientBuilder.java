package org.patientview.importer.builder;

import generated.Patientview;
import generated.Sex;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.persistence.model.enums.IdentifierTypes;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * This is going to mapping between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by james@solidstategroup.com
 * Created on 26/08/2014
 */
public class PatientBuilder {

    private Patientview oldPatient;
    private ResourceReference practitionerReference;

    public PatientBuilder(Patientview oldPatient, ResourceReference practitionerReference) {
        this.oldPatient = oldPatient;
        this.practitionerReference = practitionerReference;
    }

    public Patient build() {
        Patient newPatient = new Patient();
        createHumanName(newPatient, oldPatient);
        createDateOfBirth(newPatient, oldPatient);
        createGender(newPatient, oldPatient);
        createAddress(newPatient, oldPatient);
        createContactDetails(newPatient, oldPatient);
        addIdentifiers(newPatient, oldPatient);
        addCareProvider(newPatient);
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

    private Patient createDateOfBirth(Patient newPatient, Patientview oldPatient) {
        XMLGregorianCalendar dateofBirth = oldPatient.getPatient().getPersonaldetails().getDateofbirth();
        newPatient.setBirthDateSimple(new DateAndTime(dateofBirth.toGregorianCalendar().getTime()));
        return newPatient;
    }

    private Patient createGender(Patient newPatient, Patientview oldPatient) {
        CodeableConcept gender = new CodeableConcept();
        Sex sex = oldPatient.getPatient().getPersonaldetails().getSex();
        if (sex != null) {
            gender.setTextSimple(oldPatient.getPatient().getPersonaldetails().getSex().value());
        }
        gender.addCoding().setDisplaySimple("gender");
        newPatient.setGender(gender);
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

    private Patient addIdentifiers(Patient newPatient, Patientview oldPatient) {
        // NHS Number
        Identifier nhsNumber = newPatient.addIdentifier();
        nhsNumber.setLabelSimple(IdentifierTypes.NHS_NUMBER.toString());
        nhsNumber.setValueSimple(oldPatient.getPatient().getPersonaldetails().getNhsno());

        // Hospital Number
        Identifier hospitalNumber = newPatient.addIdentifier();
        hospitalNumber.setLabelSimple(IdentifierTypes.HOSPITAL_NUMBER.toString());
        hospitalNumber.setValueSimple(oldPatient.getPatient().getPersonaldetails().getHospitalnumber());
        return newPatient;
    }

    private Patient addCareProvider(Patient newPatient) {
        if (practitionerReference != null) {
            ResourceReference careProvider = newPatient.addCareProvider();
            careProvider.setReference(practitionerReference.getReference());
            careProvider.setDisplay(practitionerReference.getDisplay());
        }
        return newPatient;
    }

}
