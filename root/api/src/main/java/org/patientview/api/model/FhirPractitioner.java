package org.patientview.api.model;

import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Practitioner;
import org.patientview.persistence.model.BaseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class FhirPractitioner extends BaseModel {

    private String name;
    private String gender;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String postcode;
    private List<String> phoneNumbers;

    public FhirPractitioner() {
    }

    public FhirPractitioner(Practitioner practitioner) {

        // name stored in family name
        if (practitioner.getName() != null) {
            setName(practitioner.getName().getFamily().get(0).getValue());
        }

        // address from first record, if present
        if (practitioner.getAddress() != null) {
            Address address = practitioner.getAddress();
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
        if (!practitioner.getTelecom().isEmpty()) {
            for (Contact telecom : practitioner.getTelecom()) {
                getPhoneNumbers().add(telecom.getValueSimple());
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
