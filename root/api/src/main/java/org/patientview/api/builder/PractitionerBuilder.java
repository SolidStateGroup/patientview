package org.patientview.api.builder;

import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Practitioner;
import org.patientview.api.aspect.SecurityAspect;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirContact;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Build Practitioner object, suitable for insertion/update into FHIR. Handles update and create, with assumption that
 * empty strings means clear existing data, null strings means leave alone and do not update.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 04/03/2016
 */
public class PractitionerBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityAspect.class);

    private Practitioner practitioner;
    private FhirPractitioner fhirPractitioner;
    private boolean update;

    public PractitionerBuilder(Practitioner practitioner, FhirPractitioner fhirPractitioner) {
        this.practitioner = practitioner;
        this.fhirPractitioner = fhirPractitioner;
    }

    public Practitioner build() {
        if (practitioner == null) {
            practitioner = new Practitioner();
            this.update = false;
        } else {
            this.update = true;
        }

        createAddress();
        createContactDetails();
        createHumanName();
        createRole();
        return this.practitioner;
    }

    private void createAddress() {
        if (update) {
            if (fhirPractitioner.getAddress1() != null || fhirPractitioner.getAddress2() != null
                    || fhirPractitioner.getAddress3() != null || fhirPractitioner.getAddress4() != null
                    || fhirPractitioner.getPostcode() != null) {
                Address address;
                if (practitioner.getAddress() == null) {
                    address = new Address();
                } else {
                    address = practitioner.getAddress();
                }

                // address1
                if (fhirPractitioner.getAddress1() != null) {
                    if (StringUtils.isNotEmpty(fhirPractitioner.getAddress1())) {
                        if (!CollectionUtils.isEmpty(address.getLine())) {
                            address.getLine().get(0).setValue(CommonUtils.cleanSql(fhirPractitioner.getAddress1()));
                        } else {
                            address.addLineSimple(CommonUtils.cleanSql(fhirPractitioner.getAddress1()));
                        }
                    } else {
                        if (!CollectionUtils.isEmpty(address.getLine())) {
                            address.getLine().clear();
                        }
                    }
                }

                // address2
                if (fhirPractitioner.getAddress2() != null) {
                    if (StringUtils.isNotEmpty(fhirPractitioner.getAddress2())) {
                        address.setCitySimple(CommonUtils.cleanSql(fhirPractitioner.getAddress2()));
                    } else {
                        address.setCitySimple(null);
                    }
                }

                // address3
                if (fhirPractitioner.getAddress3() != null) {
                    if (StringUtils.isNotEmpty(fhirPractitioner.getAddress3())) {
                        address.setStateSimple(CommonUtils.cleanSql(fhirPractitioner.getAddress3()));
                    } else {
                        address.setStateSimple(null);
                    }
                }

                // address4
                if (fhirPractitioner.getAddress4() != null) {
                    if (StringUtils.isNotEmpty(fhirPractitioner.getAddress4())) {
                        address.setCountrySimple(CommonUtils.cleanSql(fhirPractitioner.getAddress4()));
                    } else {
                        address.setCountrySimple(null);
                    }
                }

                // postcode
                if (fhirPractitioner.getPostcode() != null) {
                    if (StringUtils.isNotEmpty(fhirPractitioner.getPostcode())) {
                        address.setZipSimple(CommonUtils.cleanSql(fhirPractitioner.getPostcode()));
                    } else {
                        address.setZipSimple(null);
                    }
                }

                // handle all blank (clear address)
                if (StringUtils.isEmpty(fhirPractitioner.getAddress1())
                        && StringUtils.isEmpty(fhirPractitioner.getAddress2())
                        && StringUtils.isEmpty(fhirPractitioner.getAddress3())
                        && StringUtils.isEmpty(fhirPractitioner.getAddress4())
                        && StringUtils.isEmpty(fhirPractitioner.getPostcode())) {
                    practitioner.setAddress(null);
                }
            }
        } else {
            if (fhirPractitioner.getAddress1() != null || fhirPractitioner.getAddress2() != null
                    || fhirPractitioner.getAddress3() != null || fhirPractitioner.getAddress4() != null
                    || fhirPractitioner.getPostcode() != null) {
                Address address = new Address();
                if (StringUtils.isNotEmpty(fhirPractitioner.getAddress1())) {
                    address.addLineSimple(CommonUtils.cleanSql(fhirPractitioner.getAddress1()));
                }
                if (StringUtils.isNotEmpty(fhirPractitioner.getAddress2())) {
                    address.setCitySimple(CommonUtils.cleanSql(fhirPractitioner.getAddress2()));
                }
                if (StringUtils.isNotEmpty(fhirPractitioner.getAddress3())) {
                    address.setStateSimple(CommonUtils.cleanSql(fhirPractitioner.getAddress3()));
                }
                if (StringUtils.isNotEmpty(fhirPractitioner.getAddress4())) {
                    address.setCountrySimple(CommonUtils.cleanSql(fhirPractitioner.getAddress4()));
                }
                if (StringUtils.isNotEmpty(fhirPractitioner.getPostcode())) {
                    address.setZipSimple(CommonUtils.cleanSql(fhirPractitioner.getPostcode()));
                }
                practitioner.setAddress(address);
            }
        }
    }

    private void createContactDetails() {
        if (!CollectionUtils.isEmpty(fhirPractitioner.getContacts())) {
            if (update) {
                if (CollectionUtils.isEmpty(practitioner.getTelecom())) {
                    // no current contacts, just add
                    for (FhirContact fhirContact : fhirPractitioner.getContacts()) {
                        if (StringUtils.isNotEmpty(fhirContact.getValue())) {
                            createNewContact(fhirContact);
                        }
                    }
                } else {
                    // store list of telecom to remove (blank value but system/use set)
                    List<Contact> toRemove = new ArrayList<>();

                    // assume contacts of one system/use, e.g. one phone/home, one email/work
                    for (FhirContact fhirContact : fhirPractitioner.getContacts()) {
                        boolean newContact = true;
                        for (Contact contact : practitioner.getTelecom()) {
                            try {
                                if (Util.isInEnum(fhirContact.getSystem(), Contact.ContactSystem.class)
                                        && Util.isInEnum(fhirContact.getUse(), Contact.ContactUse.class)
                                        && contact.getSystemSimple() != null
                                        && contact.getUseSimple() != null
                                        && contact.getSystemSimple().equals(
                                            Contact.ContactSystem.fromCode(fhirContact.getSystem()))
                                        && contact.getUseSimple().equals(
                                            Contact.ContactUse.fromCode(fhirContact.getUse()))) {
                                    if (StringUtils.isNotEmpty(fhirContact.getValue())) {
                                        // system and use exist and match, update value
                                        contact.setValueSimple(CommonUtils.cleanSql(fhirContact.getValue()));
                                    } else {
                                        // value is blank, add to list to be removed
                                        toRemove.add(contact);
                                    }
                                    newContact = false;
                                }
                            } catch (Exception e) {
                                // called when code does not match
                                LOG.info("Error updating practitioner telecom: " + e.getMessage() + ", continuing");
                            }
                        }

                        if (newContact) {
                            // brand new contact, doesn't match existing system/use
                            if (StringUtils.isNotEmpty(fhirContact.getValue())) {
                                createNewContact(fhirContact);
                            }
                        }
                    }

                    // remove blank value contacts from practitioner telecom
                    practitioner.getTelecom().removeAll(toRemove);
                }
            } else {
                for (FhirContact fhirContact : fhirPractitioner.getContacts()) {
                    if (StringUtils.isNotEmpty(fhirContact.getValue())) {
                        createNewContact(fhirContact);
                    }
                }
            }
        }
    }

    private void createHumanName() {
        HumanName humanName;
        if (update) {
            if (practitioner.getName() == null) {
                humanName = new HumanName();
                Enumeration<HumanName.NameUse> nameUse = new Enumeration(HumanName.NameUse.usual);
                humanName.setUse(nameUse);
            } else {
                humanName = practitioner.getName();
            }

            // do not clear name (unlike patient)
            if (StringUtils.isNotEmpty(fhirPractitioner.getName())) {
                // update or create family name
                if (CollectionUtils.isEmpty(humanName.getFamily())) {
                    humanName.addFamilySimple(CommonUtils.cleanSql(fhirPractitioner.getName()));
                } else {
                    humanName.getFamily().get(0).setValue(CommonUtils.cleanSql(fhirPractitioner.getName()));
                }
            }
        } else {
            humanName = new HumanName();
            // surname
            if (StringUtils.isNotEmpty(fhirPractitioner.getName())) {
                humanName.addFamilySimple(CommonUtils.cleanSql(fhirPractitioner.getName()));
            }
        }

        practitioner.setName(humanName);
    }

    private void createNewContact(FhirContact fhirContact) {
        Contact contact = practitioner.addTelecom();
        contact.setValueSimple(CommonUtils.cleanSql(fhirContact.getValue()));

        if (Util.isInEnum(fhirContact.getSystem(), Contact.ContactSystem.class)) {
            contact.setSystem(new Enumeration<>(
                    Contact.ContactSystem.valueOf(fhirContact.getSystem())));
        }

        if (Util.isInEnum(fhirContact.getUse(), Contact.ContactUse.class)) {
            contact.setUse(new Enumeration<>(
                    Contact.ContactUse.valueOf(fhirContact.getUse())));
        }
    }

    private void createRole() {
        // assume only one role
        if (update) {
            if (fhirPractitioner.getRole() != null) {
                if (StringUtils.isNotEmpty(fhirPractitioner.getRole())) {
                    if (!CollectionUtils.isEmpty(practitioner.getRole())) {
                        practitioner.getRole().clear();
                    }
                    practitioner.addRole().setTextSimple(CommonUtils.cleanSql(fhirPractitioner.getRole()));
                }
            }
        } else {
            if (StringUtils.isNotEmpty(fhirPractitioner.getRole())) {
                practitioner.addRole().setTextSimple(CommonUtils.cleanSql(fhirPractitioner.getRole()));
            }
        }
    }
}
