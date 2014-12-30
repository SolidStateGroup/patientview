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
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.enums.IdentifierTypes;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * This is going to mapping between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by james@solidstategroup.com
 * Created on 26/08/2014
 */
public class PatientBuilder {

    private Patientview data;
    private ResourceReference practitionerReference;

    public PatientBuilder(Patientview data, ResourceReference practitionerReference) {
        this.data = data;
        this.practitionerReference = practitionerReference;
    }

    public Patient build() {
        Patient newPatient = new Patient();
        createHumanName(newPatient, data);
        createDateOfBirth(newPatient, data);
        createGender(newPatient, data);
        createAddress(newPatient, data);
        createContactDetails(newPatient, data);
        addIdentifiers(newPatient, data);
        addCareProvider(newPatient);
        return newPatient;
    }

    private Patient createHumanName(Patient newPatient, Patientview data) {
        HumanName humanName = newPatient.addName();
        if (data.getPatient().getPersonaldetails() != null) {
            if (StringUtils.isNotEmpty(data.getPatient().getPersonaldetails().getSurname())) {
                humanName.addFamilySimple(data.getPatient().getPersonaldetails().getSurname());
            }
            if (StringUtils.isNotEmpty(data.getPatient().getPersonaldetails().getForename())) {
                humanName.addGivenSimple(data.getPatient().getPersonaldetails().getForename());
            }
        }
        Enumeration<HumanName.NameUse> nameUse = new Enumeration(HumanName.NameUse.usual);
        humanName.setUse(nameUse);
        return newPatient;
    }

    private Patient createDateOfBirth(Patient newPatient, Patientview data) {
        if (data.getPatient().getPersonaldetails() != null) {
            if (data.getPatient().getPersonaldetails().getDateofbirth() != null) {
                XMLGregorianCalendar dateofBirth = data.getPatient().getPersonaldetails().getDateofbirth();
                newPatient.setBirthDateSimple(new DateAndTime(dateofBirth.toGregorianCalendar().getTime()));
            }
        }
        return newPatient;
    }

    private Patient createGender(Patient newPatient, Patientview data) {
        if (data.getPatient().getPersonaldetails() != null) {
            CodeableConcept gender = new CodeableConcept();
            Sex sex = data.getPatient().getPersonaldetails().getSex();
            if (sex != null) {
                gender.setTextSimple(data.getPatient().getPersonaldetails().getSex().value());
            }
            gender.addCoding().setDisplaySimple("gender");
            newPatient.setGender(gender);
        }
        return newPatient;
    }

    private Patient createAddress(Patient newPatient, Patientview data) {
        if (data.getPatient().getPersonaldetails() != null) {
            Address address = newPatient.addAddress();
            address.addLineSimple(data.getPatient().getPersonaldetails().getAddress1());
            address.setCitySimple(data.getPatient().getPersonaldetails().getAddress2());
            address.setStateSimple(data.getPatient().getPersonaldetails().getAddress3());
            address.setCountrySimple(data.getPatient().getPersonaldetails().getAddress4());
            address.setZipSimple(data.getPatient().getPersonaldetails().getPostcode());
        }
        return newPatient;
    }

    private Patient createContactDetails(Patient newPatient, Patientview data) {
        if (data.getPatient().getPersonaldetails() != null) {
            Patient.ContactComponent contactComponent = newPatient.addContact();

            if (StringUtils.isNotEmpty(data.getPatient().getPersonaldetails().getMobile())) {
                Contact contact = contactComponent.addTelecom();
                contact.setValueSimple(data.getPatient().getPersonaldetails().getMobile());
                contact.setSystem(new Enumeration<>(Contact.ContactSystem.phone));
                contact.setUse(new Enumeration<>(Contact.ContactUse.mobile));
            }

            if (StringUtils.isNotEmpty(data.getPatient().getPersonaldetails().getTelephone1())) {
                Contact contact = contactComponent.addTelecom();
                contact.setValueSimple(data.getPatient().getPersonaldetails().getTelephone1());
                contact.setSystem(new Enumeration<>(Contact.ContactSystem.phone));
                contact.setUse(new Enumeration<>(Contact.ContactUse.home));
            }

            if (StringUtils.isNotEmpty(data.getPatient().getPersonaldetails().getTelephone2())) {
                Contact contact = contactComponent.addTelecom();
                contact.setValueSimple(data.getPatient().getPersonaldetails().getTelephone2());
                contact.setSystem(new Enumeration<>(Contact.ContactSystem.phone));
                contact.setUse(new Enumeration<>(Contact.ContactUse.home));
            }
        }
        return  newPatient;
    }

    private Patient addIdentifiers(Patient newPatient, Patientview data) {
        if (data.getPatient().getPersonaldetails() != null) {

            String identifierText = data.getPatient().getPersonaldetails().getNhsno();

            // Identifier
            Identifier identifier = newPatient.addIdentifier();
            identifier.setLabelSimple(CommonUtils.getIdentifierType(identifierText).toString());
            identifier.setValueSimple(identifierText);

            // Hospital Number
            Identifier hospitalNumber = newPatient.addIdentifier();
            hospitalNumber.setLabelSimple(IdentifierTypes.HOSPITAL_NUMBER.toString());
            hospitalNumber.setValueSimple(data.getPatient().getPersonaldetails().getHospitalnumber());
        }
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
