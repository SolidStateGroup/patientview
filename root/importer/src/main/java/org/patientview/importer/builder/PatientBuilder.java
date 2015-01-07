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
                humanName.addFamilySimple(CommonUtils.cleanSql(data.getPatient().getPersonaldetails().getSurname()));
            }
            if (StringUtils.isNotEmpty(data.getPatient().getPersonaldetails().getForename())) {
                humanName.addGivenSimple(CommonUtils.cleanSql(data.getPatient().getPersonaldetails().getForename()));
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
                gender.setTextSimple(CommonUtils.cleanSql(data.getPatient().getPersonaldetails().getSex().value()));
            }
            gender.addCoding().setDisplaySimple("gender");
            newPatient.setGender(gender);
        }
        return newPatient;
    }

    private Patient createAddress(Patient newPatient, Patientview data) {
        Patientview.Patient.Personaldetails personaldetails = data.getPatient().getPersonaldetails();
        if (personaldetails != null) {
            if (StringUtils.isNotEmpty(personaldetails.getAddress1())
                    || StringUtils.isNotEmpty(personaldetails.getAddress2())
                    || StringUtils.isNotEmpty(personaldetails.getAddress3())
                    || StringUtils.isNotEmpty(personaldetails.getAddress4())
                    || StringUtils.isNotEmpty(personaldetails.getPostcode())) {
                Address address = newPatient.addAddress();
                if (StringUtils.isNotEmpty(personaldetails.getAddress1())) {
                    address.addLineSimple(CommonUtils.cleanSql(personaldetails.getAddress1()));
                }
                if (StringUtils.isNotEmpty(personaldetails.getAddress2())) {
                    address.setCitySimple(CommonUtils.cleanSql(personaldetails.getAddress2()));
                }
                if (StringUtils.isNotEmpty(personaldetails.getAddress3())) {
                    address.setStateSimple(CommonUtils.cleanSql(personaldetails.getAddress3()));
                }
                if (StringUtils.isNotEmpty(personaldetails.getAddress4())) {
                    address.setCountrySimple(CommonUtils.cleanSql(personaldetails.getAddress4()));
                }
                if (StringUtils.isNotEmpty(personaldetails.getPostcode())) {
                    address.setZipSimple(CommonUtils.cleanSql(personaldetails.getPostcode()));
                }
            }
        }
        return newPatient;
    }

    private Patient createContactDetails(Patient newPatient, Patientview data) {
        Patientview.Patient.Personaldetails personaldetails = data.getPatient().getPersonaldetails();
        if (personaldetails != null) {
            if (StringUtils.isNotEmpty(personaldetails.getMobile())
                    || StringUtils.isNotEmpty(personaldetails.getTelephone1())
                    || StringUtils.isNotEmpty(personaldetails.getTelephone2())) {
                Patient.ContactComponent contactComponent = newPatient.addContact();

                if (StringUtils.isNotEmpty(personaldetails.getMobile())) {
                    Contact contact = contactComponent.addTelecom();
                    contact.setValueSimple(CommonUtils.cleanSql(personaldetails.getMobile()));
                    contact.setSystem(new Enumeration<>(Contact.ContactSystem.phone));
                    contact.setUse(new Enumeration<>(Contact.ContactUse.mobile));
                }

                if (StringUtils.isNotEmpty(personaldetails.getTelephone1())) {
                    Contact contact = contactComponent.addTelecom();
                    contact.setValueSimple(CommonUtils.cleanSql(personaldetails.getTelephone1()));
                    contact.setSystem(new Enumeration<>(Contact.ContactSystem.phone));
                    contact.setUse(new Enumeration<>(Contact.ContactUse.home));
                }

                if (StringUtils.isNotEmpty(personaldetails.getTelephone2())) {
                    Contact contact = contactComponent.addTelecom();
                    contact.setValueSimple(CommonUtils.cleanSql(personaldetails.getTelephone2()));
                    contact.setSystem(new Enumeration<>(Contact.ContactSystem.phone));
                    contact.setUse(new Enumeration<>(Contact.ContactUse.home));
                }
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
            if (StringUtils.isNotEmpty(data.getPatient().getPersonaldetails().getHospitalnumber())) {
                Identifier hospitalNumber = newPatient.addIdentifier();
                hospitalNumber.setLabelSimple(IdentifierTypes.HOSPITAL_NUMBER.toString());
                hospitalNumber.setValueSimple(
                        CommonUtils.cleanSql(data.getPatient().getPersonaldetails().getHospitalnumber()));
            }
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
