package org.patientview.persistence.model;

import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Identifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class FhirPatient extends BaseModel {

    private String forename;
    private String surname;
    private Date dateOfBirth;
    private String gender;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String postcode;
    private List<String> phoneNumbers;
    private List<FhirIdentifier> identifiers;

    public FhirPatient() {
    }

    public FhirPatient(org.hl7.fhir.instance.model.Patient patient) {

        // name from first recorded name record, if present
        if (!patient.getName().isEmpty()) {
            if (!patient.getName().get(0).getGiven().isEmpty()) {
                setForename(patient.getName().get(0).getGiven().get(0).getValue());
            }
            if (!patient.getName().get(0).getFamily().isEmpty()) {
                setSurname(patient.getName().get(0).getFamily().get(0).getValue());
            }
        }

        // date of birth if present
        DateAndTime fhirDateOfBirth = patient.getBirthDateSimple();
        if (fhirDateOfBirth != null) {
            setDateOfBirth(new Date(new GregorianCalendar(fhirDateOfBirth.getYear(), fhirDateOfBirth.getMonth() - 1,
                    fhirDateOfBirth.getDay()).getTimeInMillis()));
        }

        // gender/sex
        if (patient.getGender() != null) {
            setGender(patient.getGender().getTextSimple());
        }

        // address from first record, if present
        if (!patient.getAddress().isEmpty()) {
            Address address = patient.getAddress().get(0);
            if (!address.getLine().isEmpty()) {
                setAddress1(address.getLine().get(0).getValue());
            }
            setAddress2(address.getCitySimple());
            setAddress3(address.getStateSimple());
            setAddress4(address.getCountrySimple());
            setPostcode(address.getZipSimple());
        }

        // phone numbers
        setPhoneNumbers(new ArrayList<String>());
        if (!patient.getContact().isEmpty()) {
            for (Contact telecom : patient.getContact().get(0).getTelecom()) {
                getPhoneNumbers().add(telecom.getValueSimple());
            }
        }

        // identifiers if present
        setIdentifiers(new ArrayList<FhirIdentifier>());
        if (!patient.getIdentifier().isEmpty()) {
            for (Identifier identifier : patient.getIdentifier()) {
                getIdentifiers().add(new FhirIdentifier(identifier));
            }
        }

    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getAddress4() {
        return address4;
    }

    public void setAddress4(String address4) {
        this.address4 = address4;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public List<FhirIdentifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<FhirIdentifier> identifiers) {
        this.identifiers = identifiers;
    }
}
