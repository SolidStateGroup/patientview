package org.patientview.importer.builder;

import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Practitioner;
import org.patientview.config.utils.CommonUtils;

/**
 * This maps between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by jamesr@solidstategroup.com
 * Created on 26/08/2014
 */
public class PractitionerBuilder {

    private Patientview data;

    public PractitionerBuilder(Patientview data) {
        this.data = data;
    }

    public Practitioner build() {

        Practitioner practitioner = new Practitioner();
        Patientview.Gpdetails gp = data.getGpdetails();

        if (gp != null) {
            HumanName humanName = new HumanName();
            humanName.addFamilySimple(CommonUtils.cleanSql(gp.getGpname()));
            Enumeration<HumanName.NameUse> nameUse = new Enumeration<>(HumanName.NameUse.usual);
            humanName.setUse(nameUse);
            practitioner.setName(humanName);

            boolean addAddress = false;
            Address address = new Address();
            if (StringUtils.isNotEmpty(gp.getGpaddress1())) {
                address.addLineSimple(CommonUtils.cleanSql(gp.getGpaddress1()));
                addAddress = true;
            }
            if (StringUtils.isNotEmpty(gp.getGpaddress2())) {
                address.setCitySimple(CommonUtils.cleanSql(gp.getGpaddress2()));
                addAddress = true;
            }
            if (StringUtils.isNotEmpty(gp.getGpaddress3())) {
                address.setStateSimple(CommonUtils.cleanSql(gp.getGpaddress3()));
                addAddress = true;
            }
            if (StringUtils.isNotEmpty(gp.getGpaddress4())) {
                address.setCountrySimple(CommonUtils.cleanSql(gp.getGpaddress4()));
                addAddress = true;
            }
            if (StringUtils.isNotEmpty(gp.getGppostcode())) {
                address.setZipSimple(CommonUtils.cleanSql(gp.getGppostcode()));
                addAddress = true;
            }
            if (addAddress) {
                practitioner.setAddress(address);
            }

            if (StringUtils.isNotEmpty(gp.getGptelephone())) {
                Contact contact = practitioner.addTelecom();
                contact.setSystem(new Enumeration<>(Contact.ContactSystem.phone));
                contact.setValueSimple(gp.getGptelephone());
                contact.setUse(new Enumeration<>(Contact.ContactUse.work));
            }

            if (StringUtils.isNotEmpty(gp.getGpemail())) {
                Contact contact = practitioner.addTelecom();
                contact.setSystem(new Enumeration<>(Contact.ContactSystem.email));
                contact.setValueSimple(CommonUtils.cleanSql(gp.getGpemail()));
                contact.setUse(new Enumeration<>(Contact.ContactUse.work));
            }
        }

        return practitioner;
    }
}
