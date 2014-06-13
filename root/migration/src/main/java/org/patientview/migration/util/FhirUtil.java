package org.patientview.migration.util;

import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Decimal;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Instant;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.String_;
import org.hl7.fhir.instance.model.Type;
import org.patientview.model.Patient;
import org.patientview.patientview.model.TestResult;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public final class FhirUtil {

    private FhirUtil() {

    }

    public static org.hl7.fhir.instance.model.Patient getFhirPatient(Patient patient) {


        org.hl7.fhir.instance.model.Patient fhirPatient = new org.hl7.fhir.instance.model.Patient();

        if (patient.getDob() != null) {
            fhirPatient.setBirthDate(getDateTime(patient.getDob()));
        }
        fhirPatient.setGender(getCodeableConcept(patient.getSex()));
        fhirPatient.getName().add(getHumanName(patient));
        fhirPatient.getIdentifier().add(getIdentifer(patient.getNhsno()));
        fhirPatient.getAddress().add(FhirUtil.getPatientAddress(patient));
        fhirPatient.getContact().add(getTelephoneContactComponent(patient));
        fhirPatient.getContact().add(getEmailContactComponent(patient));
        return fhirPatient;

    }

    public static Observation getObservation(TestResult testResult, String uuid) throws Exception {
        Observation observation = new Observation();

        observation.setStatus(getEnumeration(Observation.ObservationStatus.registered));
        observation.setName(getCodeableConcept(testResult.getTestcode()));
        observation.setValue(getType(testResult.getValue()));
        observation.setSubject(getResourceReference(uuid));
        observation.setIssued(getInstant(testResult.getTimestamp().getTime()));
        observation.setReliability(getEnumeration(Observation.ObservationReliability.ok));
        return observation;

    }

    private static org.hl7.fhir.instance.model.Patient.ContactComponent getEmailContactComponent(Patient patient) {
        org.hl7.fhir.instance.model.Patient.ContactComponent contactComponent = new org.hl7.fhir.instance.model.Patient.ContactComponent();
        Contact contact = new Contact();
        contact.setValue(wrapString(patient.getTelephone1()));
        contact.setSystem(new Enumeration<Contact.ContactSystem>(Contact.ContactSystem.phone));

        contactComponent.addTelecom().setSystem(new Enumeration<Contact.ContactSystem>(Contact.ContactSystem.email));
        contactComponent.addTelecom().setValue(wrapString(patient.getEmailAddress()));
        return contactComponent;
    }


    private static Address getPatientAddress(Patient patient) {

        Address address = new Address();
        address.addLineSimple(patient.getAddress1());
        address.setCitySimple(patient.getAddress2());
        address.setStateSimple(patient.getAddress3());
        address.setCountrySimple(patient.getAddress4());
        address.setZipSimple(patient.getPostcode());

        return address;
    }


    private static CodeableConcept getCodeableConcept(String code) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setText(wrapString(code));
        codeableConcept.setTextSimple(code);

        return codeableConcept;
    }

    private static HumanName getHumanName(Patient patient) {
        HumanName humanName = new HumanName();
        humanName.addFamilySimple(patient.getSurname());
        humanName.addGivenSimple(patient.getForename());
        return humanName;

    }

    private static Identifier getIdentifer(String nhsno) {

        Identifier identifier = new Identifier();
        identifier.setValue(wrapString(nhsno));
        identifier.setLabel(wrapString("nhsno"));

        return identifier;

    }

    private static Instant getInstant(Date date) {
        DateAndTime dateAndTime = new DateAndTime(date);
        Instant instant = new Instant();
        //instant.setValue(dateAndTime);
        return instant;
    }


    private static org.hl7.fhir.instance.model.Patient.ContactComponent getTelephoneContactComponent(Patient patient) {
        org.hl7.fhir.instance.model.Patient.ContactComponent contactComponent = new org.hl7.fhir.instance.model.Patient.ContactComponent();
        Contact contact = new Contact();
        contact.setValue(wrapString(patient.getTelephone1()));
        contact.setSystem(new Enumeration<Contact.ContactSystem>(Contact.ContactSystem.phone));

        contactComponent.addTelecom().setSystem(new Enumeration<Contact.ContactSystem>(Contact.ContactSystem.phone));
        contactComponent.addTelecom().setValue(wrapString(patient.getTelephone1()));
        return contactComponent;
    }


    private static Organization getOrganization() throws Exception {
        return null;
    }

    private static <T extends Enum> Enumeration getEnumeration(T value) {
        Enumeration enumeration = new Enumeration<T>(value);
        return enumeration;
    }

    private static ResourceReference getResourceReference(String uuid) {
        ResourceReference resourceReference = new ResourceReference();
        resourceReference.setReference(wrapString("patientUuid"));
        resourceReference.setDisplay(wrapString(uuid));
        return  resourceReference;
    }


    private static String_ wrapString(String string) {
        String_ string_ = new String_();
        string_.setValue(string);
        return string_;
    }


    private static Type getType(String value) {
        Quantity type = new Quantity();
        Decimal decimal = new Decimal();
        decimal.setValue(new BigDecimal(value));
        type.setValue(decimal);
        return type;
    }

    private static DateTime getDateTime(Date date) {
        DateTime dateTime = new DateTime();

        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            //dateTime.setValue(simpleDateFormat.format(date));
        }

        return dateTime;
    }

}
