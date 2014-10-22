package org.patientview.importer.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Organization;

/**
 * This maps between parameters from old PatientView and the new PatientView fhir record
 *
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class OrganizationBuilder {

    private Patientview data;

    public OrganizationBuilder(Patientview data) {
        this.data = data;
    }

    public Organization build() {

        Organization organization = new Organization();
        Patientview.Centredetails centre = data.getCentredetails();
        centre.setCentrename(centre.getCentrename());

        Identifier identifier = organization.addIdentifier();
        identifier.setValueSimple(centre.getCentrecode());
        identifier.setLabelSimple("CODE");

        Address address = organization.addAddress();
        address.addLineSimple(centre.getCentreaddress1());
        address.setCitySimple(centre.getCentreaddress2());
        address.setStateSimple(centre.getCentreaddress3());
        address.setCountrySimple(centre.getCentreaddress4());
        address.setZipSimple(centre.getCentrepostcode());

        Contact telephone = organization.addTelecom();
        telephone.setValueSimple(centre.getCentretelephone());
        telephone.setSystem(new Enumeration(Contact.ContactSystem.phone));

        Contact email = organization.addTelecom();
        email.setValueSimple(centre.getCentretelephone());
        email.setSystem(new Enumeration(Contact.ContactSystem.email));

        return organization;
    }
}
