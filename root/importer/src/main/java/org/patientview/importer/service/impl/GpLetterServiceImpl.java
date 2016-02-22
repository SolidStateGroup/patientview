package org.patientview.importer.service.impl;

import com.itextpdf.text.DocumentException;
import generated.Patientview;
import org.patientview.importer.service.GpLetterService;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.service.GpLetterCreationService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Service
public class GpLetterServiceImpl extends AbstractServiceImpl<GpLetterServiceImpl> implements GpLetterService {

    @Inject
    private GpLetterRepository gpLetterRepository;

    @Inject
    private GpMasterRepository gpMasterRepository;

    @Inject
    private GpLetterCreationService gpLetterCreationService;

    @Inject
    Properties properties;

    @Override
    @Transactional
    public void add(Patientview patientview, Group sourceGroup) {
        Patientview.Gpdetails gp = patientview.getGpdetails();
        Patientview.Patient patient = patientview.getPatient();

        GpLetter gpLetter = new GpLetter();
        gpLetter.setCreated(new Date());

        // patient details
        gpLetter.setPatientForename(patient.getPersonaldetails().getForename());
        gpLetter.setPatientSurname(patient.getPersonaldetails().getSurname());
        if (patient.getPersonaldetails().getDateofbirth() != null) {
            gpLetter.setPatientDateOfBirth(
                    patient.getPersonaldetails().getDateofbirth().toGregorianCalendar().getTime());
        }

        // set identifier trimmed without spaces
        gpLetter.setPatientIdentifier(patient.getPersonaldetails().getNhsno().trim().replace(" ", ""));

        // gp details
        gpLetter.setGpName(gp.getGpname());
        gpLetter.setGpAddress1(gp.getGpaddress1());
        gpLetter.setGpAddress2(gp.getGpaddress2());
        gpLetter.setGpAddress3(gp.getGpaddress3());
        gpLetter.setGpAddress4(gp.getGpaddress4());
        gpLetter.setGpPostcode(gp.getGppostcode());

        // signup key (generated)
        gpLetter.setSignupKey(gpLetterCreationService.generateSignupKey());

        // source group (provided the xml, from fhirlink)
        gpLetter.setSourceGroup(sourceGroup);

        // letter (generated)
        try {
            // if not enough information to produce letter address then use gp master
            if (!hasValidPracticeDetails(patientview)) {
                List<GpMaster> gpMasters = gpMasterRepository.findByPostcode(gp.getGppostcode().replace(" ", ""));
                if (!gpMasters.isEmpty()) {
                    gpLetter.setLetterContent(gpLetterCreationService.generateLetter(
                            gpLetter, gpMasters.get(0),
                            properties.getProperty("site.url"), properties.getProperty("gp.letter.output.directory")));
                } else {
                    gpLetter.setLetterContent(gpLetterCreationService.generateLetter(
                            gpLetter, gpMasters.get(0),
                            properties.getProperty("site.url"), properties.getProperty("gp.letter.output.directory")));
                }
            } else {
                gpLetter.setLetterContent(gpLetterCreationService.generateLetter(
                        gpLetter, null, properties.getProperty("site.url"),
                        properties.getProperty("gp.letter.output.directory")));
            }
        } catch (DocumentException de) {
            LOG.error("Could not generate GP letter, continuing: " + de.getMessage());
            gpLetter.setLetterContent(null);
        }

        gpLetterRepository.save(gpLetter);
    }

    @Override
    public boolean hasValidPracticeDetails(Patientview patientview) {
        // check gpdetails section is present
        if (patientview.getGpdetails() == null) {
            return false;
        }

        Patientview.Gpdetails gp = patientview.getGpdetails();

        // convert to GpLetter and check using shared service
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName(gp.getGpname());
        gpLetter.setGpAddress1(gp.getGpaddress1());
        gpLetter.setGpAddress2(gp.getGpaddress2());
        gpLetter.setGpAddress3(gp.getGpaddress3());
        gpLetter.setGpAddress4(gp.getGpaddress4());
        gpLetter.setGpPostcode(gp.getGppostcode());

        return gpLetterCreationService.hasValidPracticeDetails(gpLetter);
    }

    @Override
    public boolean hasValidPracticeDetailsSingleMaster(Patientview patientview) {
        // check gpdetails section is present
        if (patientview.getGpdetails() == null) {
            return false;
        }

        Patientview.Gpdetails gp = patientview.getGpdetails();

        // convert to GpLetter and check using shared service
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName(gp.getGpname());
        gpLetter.setGpAddress1(gp.getGpaddress1());
        gpLetter.setGpAddress2(gp.getGpaddress2());
        gpLetter.setGpAddress3(gp.getGpaddress3());
        gpLetter.setGpAddress4(gp.getGpaddress4());
        gpLetter.setGpPostcode(gp.getGppostcode());

        return gpLetterCreationService.hasValidPracticeDetailsSingleMaster(gpLetter);
    }

    @Override
    public List<GpLetter> matchByGpDetails(Patientview patientview) {
        // convert to GpLetter and check using shared service
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName(patientview.getGpdetails().getGpname());
        gpLetter.setGpAddress1(patientview.getGpdetails().getGpaddress1());
        gpLetter.setGpAddress2(patientview.getGpdetails().getGpaddress2());
        gpLetter.setGpAddress3(patientview.getGpdetails().getGpaddress3());
        gpLetter.setGpAddress4(patientview.getGpdetails().getGpaddress4());
        gpLetter.setGpPostcode(patientview.getGpdetails().getGppostcode());

        return gpLetterCreationService.matchByGpDetails(gpLetter);
    }
}
