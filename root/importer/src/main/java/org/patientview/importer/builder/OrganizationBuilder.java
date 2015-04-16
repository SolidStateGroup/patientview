package org.patientview.importer.builder;

import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Enumeration;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Organization;
import org.patientview.config.utils.CommonUtils;

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

        // will always have code
        Identifier identifier = organization.addIdentifier();
        identifier.setValueSimple(centre.getCentrecode());
        identifier.setLabelSimple("CODE");

        // name
        if (StringUtils.isNotEmpty(centre.getCentrename())) {
            organization.setNameSimple(CommonUtils.cleanSql(centre.getCentrename()));
        }

        // address
        if (StringUtils.isNotEmpty(centre.getCentreaddress1())
            || StringUtils.isNotEmpty(centre.getCentreaddress2())
            || StringUtils.isNotEmpty(centre.getCentreaddress3())
            || StringUtils.isNotEmpty(centre.getCentreaddress4())
            || StringUtils.isNotEmpty(centre.getCentrepostcode())) {
            Address address = organization.addAddress();

            if (StringUtils.isNotEmpty(centre.getCentreaddress1())) {
                address.addLineSimple(CommonUtils.cleanSql(centre.getCentreaddress1()));
            }
            if (StringUtils.isNotEmpty(centre.getCentreaddress2())) {
                address.setCitySimple(CommonUtils.cleanSql(centre.getCentreaddress2()));
            }
            if (StringUtils.isNotEmpty(centre.getCentreaddress3())) {
                address.setStateSimple(CommonUtils.cleanSql(centre.getCentreaddress3()));
            }
            if (StringUtils.isNotEmpty(centre.getCentreaddress4())) {
                address.setCountrySimple(CommonUtils.cleanSql(centre.getCentreaddress4()));
            }
            if (StringUtils.isNotEmpty(centre.getCentrepostcode())) {
                address.setZipSimple(CommonUtils.cleanSql(centre.getCentrepostcode()));
            }
        }

        // telephone
        if (StringUtils.isNotEmpty(centre.getCentretelephone())) {
            Contact telephone = organization.addTelecom();
            telephone.setValueSimple(CommonUtils.cleanSql(centre.getCentretelephone()));
            telephone.setSystem(new Enumeration(Contact.ContactSystem.phone));
        }

        // email
        if (StringUtils.isNotEmpty(centre.getCentreemail())) {
            Contact email = organization.addTelecom();
            email.setValueSimple(CommonUtils.cleanSql(centre.getCentreemail()));
            email.setSystem(new Enumeration(Contact.ContactSystem.email));
        }

        return organization;
    }
}
