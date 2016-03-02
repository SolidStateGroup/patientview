package org.patientview.api.builder;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Patient;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirContact;
import org.patientview.persistence.model.FhirIdentifier;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Build Patient object, suitable for insertion/update into FHIR. Handles update and create, with assumption that
 * empty strings means clear existing data, null strings means leave alone and do not update. For Date, clear if null.
 * Also assume that a patient has only one address.
 *
 * Created by james@solidstategroup.com
 * Created on 03/03/2015
 */
public class PatientBuilder {

    private Patient patient;
    private FhirPatient fhirPatient;
    private boolean update;

    public PatientBuilder(Patient patient, FhirPatient fhirPatient) {
        this.patient = patient;
        this.fhirPatient = fhirPatient;
    }

    public Patient build() {
        if (patient == null) {
            patient = new Patient();
            this.update = false;
        } else {
            this.update = true;
        }

        createHumanName();
        createDateOfBirth();
        createGender();
        createAddress();
        createContactDetails();
        addIdentifiers();
        /*addCareProvider(newPatient);*/
        return patient;
    }

    private void createHumanName() {
        if (update) {
            HumanName humanName;
            if (CollectionUtils.isEmpty(patient.getName())) {
                humanName = patient.addName();
                Enumeration<HumanName.NameUse> nameUse = new Enumeration(HumanName.NameUse.usual);
                humanName.setUse(nameUse);
            } else {
                humanName = patient.getName().get(0);
            }

            // forename
            if (fhirPatient.getForename() != null) {
                if (StringUtils.isNotEmpty(fhirPatient.getForename())) {
                    // update or create given name
                    if (CollectionUtils.isEmpty(humanName.getGiven())) {
                        humanName.addGivenSimple(fhirPatient.getForename());
                    } else {
                        humanName.getGiven().get(0).setValue(fhirPatient.getForename());
                    }
                } else {
                    // clear existing given name
                    if (!CollectionUtils.isEmpty(humanName.getGiven())) {
                        humanName.getGiven().clear();
                    }
                }
            }

            // surname
            if (fhirPatient.getSurname() != null) {
                if (StringUtils.isNotEmpty(fhirPatient.getSurname())) {
                    // update or create family name
                    if (CollectionUtils.isEmpty(humanName.getFamily())) {
                        humanName.addFamilySimple(fhirPatient.getSurname());
                    } else {
                        humanName.getFamily().get(0).setValue(fhirPatient.getSurname());
                    }
                } else {
                    // clear existing family name
                    if (!CollectionUtils.isEmpty(humanName.getFamily())) {
                        humanName.getFamily().clear();
                    }
                }
            }

        } else {
            HumanName humanName = patient.addName();

            // forename
            if (fhirPatient.getForename() != null) {
                humanName.addGivenSimple(CommonUtils.cleanSql(fhirPatient.getForename()));
            }

            // surname
            if (fhirPatient.getSurname() != null) {
                humanName.addFamilySimple(CommonUtils.cleanSql(fhirPatient.getSurname()));
            }
        }
    }

    private void createDateOfBirth() {
        if (fhirPatient.getDateOfBirth() != null) {
            patient.setBirthDateSimple(new DateAndTime(fhirPatient.getDateOfBirth()));
        } else {
            patient.setBirthDate(null);
        }
    }

    private void createGender() {
        if (fhirPatient.getGender() != null) {
            if (StringUtils.isNotEmpty(fhirPatient.getGender())) {
                // set gender
                CodeableConcept gender = new CodeableConcept();
                gender.setTextSimple(CommonUtils.cleanSql(fhirPatient.getGender()));
                gender.addCoding().setDisplaySimple("gender");
                patient.setGender(gender);
            } else {
                // clear existing gender
                patient.setGender(null);
            }
        }
    }

    private void createAddress() {
        if (update) {
            if (fhirPatient.getAddress1() != null || fhirPatient.getAddress2() != null
                    || fhirPatient.getAddress3() != null || fhirPatient.getAddress4() != null
                    || fhirPatient.getPostcode() != null) {
                Address address;
                if (CollectionUtils.isEmpty(patient.getAddress())) {
                    address = patient.addAddress();
                } else {
                    address = patient.getAddress().get(0);
                }

                // address1
                if (fhirPatient.getAddress1() != null) {
                    if (StringUtils.isNotEmpty(fhirPatient.getAddress1())) {
                        if (!CollectionUtils.isEmpty(address.getLine())) {
                            address.getLine().get(0).setValue(CommonUtils.cleanSql(fhirPatient.getAddress1()));
                        } else {
                            address.addLineSimple(CommonUtils.cleanSql(fhirPatient.getAddress1()));
                        }
                    } else {
                        if (!CollectionUtils.isEmpty(address.getLine())) {
                            address.getLine().clear();
                        }
                    }
                }

                // address2
                if (fhirPatient.getAddress2() != null) {
                    if (StringUtils.isNotEmpty(fhirPatient.getAddress2())) {
                        address.setCitySimple(CommonUtils.cleanSql(fhirPatient.getAddress2()));
                    } else {
                        address.setCitySimple(null);
                    }
                }

                // address3
                if (fhirPatient.getAddress3() != null) {
                    if (StringUtils.isNotEmpty(fhirPatient.getAddress3())) {
                        address.setStateSimple(CommonUtils.cleanSql(fhirPatient.getAddress3()));
                    } else {
                        address.setStateSimple(null);
                    }
                }

                // address4
                if (fhirPatient.getAddress4() != null) {
                    if (StringUtils.isNotEmpty(fhirPatient.getAddress4())) {
                        address.setCountrySimple(CommonUtils.cleanSql(fhirPatient.getAddress4()));
                    } else {
                        address.setCountrySimple(null);
                    }
                }

                // postcode
                if (fhirPatient.getPostcode() != null) {
                    if (StringUtils.isNotEmpty(fhirPatient.getPostcode())) {
                        address.setZipSimple(CommonUtils.cleanSql(fhirPatient.getPostcode()));
                    } else {
                        address.setZipSimple(null);
                    }
                }

                // handle all blank (clear address)
                if (StringUtils.isEmpty(fhirPatient.getAddress1())
                        && StringUtils.isEmpty(fhirPatient.getAddress2())
                        && StringUtils.isEmpty(fhirPatient.getAddress3())
                        && StringUtils.isEmpty(fhirPatient.getAddress4())
                        && StringUtils.isEmpty(fhirPatient.getPostcode())) {
                    patient.getAddress().clear();
                }
            }
        } else {
            if (fhirPatient.getAddress1() != null || fhirPatient.getAddress2() != null
                    || fhirPatient.getAddress3() != null || fhirPatient.getAddress4() != null
                    || fhirPatient.getPostcode() != null) {
                Address address = patient.addAddress();
                if (StringUtils.isNotEmpty(fhirPatient.getAddress1())) {
                    address.addLineSimple(CommonUtils.cleanSql(fhirPatient.getAddress1()));
                }
                if (StringUtils.isNotEmpty(fhirPatient.getAddress2())) {
                    address.setCitySimple(CommonUtils.cleanSql(fhirPatient.getAddress2()));
                }
                if (StringUtils.isNotEmpty(fhirPatient.getAddress3())) {
                    address.setStateSimple(CommonUtils.cleanSql(fhirPatient.getAddress3()));
                }
                if (StringUtils.isNotEmpty(fhirPatient.getAddress4())) {
                    address.setCountrySimple(CommonUtils.cleanSql(fhirPatient.getAddress4()));
                }
                if (StringUtils.isNotEmpty(fhirPatient.getPostcode())) {
                    address.setZipSimple(CommonUtils.cleanSql(fhirPatient.getPostcode()));
                }
            }
        }
    }

    private void createContactDetails() {
        if (!CollectionUtils.isEmpty(fhirPatient.getContacts())) {
            if (update) {
                // assume only one ContactComponent
                Patient.ContactComponent contactComponent;
                if (CollectionUtils.isEmpty(patient.getContact())) {
                    contactComponent = patient.addContact();
                } else {
                    contactComponent = patient.getContact().get(0);
                }

                // clear telecom details
                if (!CollectionUtils.isEmpty(contactComponent.getTelecom())) {
                    contactComponent.getTelecom().clear();
                }

                if (!addContactsToContactComponent(contactComponent, fhirPatient.getContacts())) {
                    // no correct contacts added
                    patient.getContact().clear();
                }
            } else {
                Patient.ContactComponent contactComponent = patient.addContact();
                if (!addContactsToContactComponent(contactComponent, fhirPatient.getContacts())) {
                    // no correct contacts added
                    patient.getContact().clear();
                }
            }
        }
    }

    private boolean addContactsToContactComponent(Patient.ContactComponent contactComponent,
                                                                   List<FhirContact> fhirContacts) {
        boolean correctContactsAdded = false;

        // iterate through FhirContacts and store if value exists and system is "phone"
        for (FhirContact fhirContact : fhirContacts) {
            if (StringUtils.isNotEmpty(fhirContact.getValue())
                    && StringUtils.isNotEmpty(fhirContact.getSystem())
                    && fhirContact.getSystem().equals(Contact.ContactSystem.phone.name())) {

                // add new telecom Contact, currently not storing any other type of Contact
                Contact contact = contactComponent.addTelecom();

                // value (e.g. phone number)
                contact.setValueSimple(CommonUtils.cleanSql(fhirContact.getValue()));

                // system (e.g. phone, fax etc), currently only support "phone"
                contact.setSystem(new Enumeration<>(Contact.ContactSystem.phone));

                // use (e.g. mobile, home, work etc)
                if (StringUtils.isNotEmpty(fhirContact.getUse())) {
                    if (fhirContact.getUse().equals(Contact.ContactUse.mobile.name())) {
                        contact.setUse(new Enumeration<>(Contact.ContactUse.mobile));
                    } else if (fhirContact.getUse().equals(Contact.ContactUse.home.name())) {
                        contact.setUse(new Enumeration<>(Contact.ContactUse.home));
                    } else if (fhirContact.getUse().equals(Contact.ContactUse.work.name())) {
                        contact.setUse(new Enumeration<>(Contact.ContactUse.work));
                    }
                }

                correctContactsAdded = true;
            }
        }
        return correctContactsAdded;
    }

    private void addIdentifiers() {
        if (!CollectionUtils.isEmpty(fhirPatient.getIdentifiers())) {
            if (update) {
                patient.getIdentifier().clear();
            }

            for (FhirIdentifier fhirIdentifier : fhirPatient.getIdentifiers()) {
                if (StringUtils.isNotEmpty(fhirIdentifier.getValue())
                        && StringUtils.isNotEmpty(fhirIdentifier.getLabel())
                        && ApiUtil.isInEnum(fhirIdentifier.getLabel(), IdentifierTypes.class)) {
                    Identifier identifier = patient.addIdentifier();
                    identifier.setValueSimple(fhirIdentifier.getValue());
                    identifier.setLabelSimple(fhirIdentifier.getLabel());
                }
            }
        }
    }

    /*private Patient addCareProvider(Patient newPatient) {
        if (practitionerReference != null) {
            ResourceReference careProvider = newPatient.addCareProvider();
            careProvider.setReference(practitionerReference.getReference());
            careProvider.setDisplay(practitionerReference.getDisplay());
        }
        return newPatient;
    }*/

}
