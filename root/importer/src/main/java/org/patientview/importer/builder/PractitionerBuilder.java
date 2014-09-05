package org.patientview.importer.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Practitioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This maps between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by jamesr@solidstategroup.com
 * Created on 26/08/2014
 */
public class PractitionerBuilder {

    private final Logger LOG = LoggerFactory.getLogger(PractitionerBuilder.class);

    private Patientview data;

    public PractitionerBuilder(Patientview data) {
        this.data = data;
    }

    public Practitioner build() {

        Practitioner practitioner = new Practitioner();
        Patientview.Gpdetails gp = data.getGpdetails();

        HumanName humanName = new HumanName();
        humanName.addFamilySimple(gp.getGpname());
        Enumeration<HumanName.NameUse> nameUse = new Enumeration(HumanName.NameUse.usual);
        humanName.setUse(nameUse);
        practitioner.setName(humanName);

        Address address = new Address();
        address.addLineSimple(gp.getGpaddress1());
        address.setCitySimple(gp.getGpaddress2());
        address.setStateSimple(gp.getGpaddress3());
        address.setZipSimple(gp.getGppostcode());
        practitioner.setAddress(address);

        Contact contact = practitioner.addTelecom();
        contact.setSystem(new Enumeration(Contact.ContactSystem.phone));
        contact.setValueSimple(gp.getGptelephone());

        return practitioner;
    }
}
