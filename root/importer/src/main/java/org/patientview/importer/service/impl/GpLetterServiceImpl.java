package org.patientview.importer.service.impl;

import com.itextpdf.text.DocumentException;
import generated.Patientview;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.patientview.importer.service.GpLetterService;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.service.GpLetterCreationService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
        gpLetter.setSignupKey(generateSignupKey());

        // source group (provided the xml, from fhirlink)
        gpLetter.setSourceGroup(sourceGroup);

        // letter (generated)
        try {
            // if not enough information to produce letter address then use gp master
            if (!hasValidPracticeDetails(patientview)) {
                List<GpMaster> gpMasters = gpMasterRepository.findByPostcode(gp.getGppostcode());
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
    private String generateSignupKey() {
        return RandomStringUtils.randomAlphanumeric(7)
                .replace("o", "p").replace("0", "2").replace("1", "3").replace("l", "m").replace("i", "j")
                .toUpperCase();
    }

    @Override
    public boolean hasValidPracticeDetails(Patientview patientview) {
        // check gpdetails section is present
        if (patientview.getGpdetails() == null) {
            return false;
        }

        Patientview.Gpdetails gp = patientview.getGpdetails();

        // check postcode is set
        if (StringUtils.isEmpty(gp.getGppostcode())) {
            return false;
        }

        // check at least one gp in master table
        if (CollectionUtils.isEmpty(gpMasterRepository.findByPostcode(gp.getGppostcode()))) {
            return false;
        }

        // validate at least 2 of address1, address2, address3 is present
        int fieldCount = 0;
        if (StringUtils.isNotEmpty(gp.getGpaddress1())) {
            fieldCount++;
        }
        if (StringUtils.isNotEmpty(gp.getGpaddress2())) {
            fieldCount++;
        }
        if (StringUtils.isNotEmpty(gp.getGpaddress3())) {
            fieldCount++;
        }

        return fieldCount > 1;
    }

    @Override
    public boolean hasValidPracticeDetailsSingleMaster(Patientview patientview) {
        // check gpdetails section is present
        if (patientview.getGpdetails() == null) {
            return false;
        }

        Patientview.Gpdetails gp = patientview.getGpdetails();

        // check postcode is set
        if (StringUtils.isEmpty(gp.getGppostcode())) {
            return false;
        }

        // validate postcode exists in GP master table and only one record
        return gpMasterRepository.findByPostcode(gp.getGppostcode()).size() == 1;
    }

    @Override
    public List<GpLetter> matchByGpDetails(Patientview patientview) {
        Set<GpLetter> matchedGpLetters = new HashSet<>();

        if (hasValidPracticeDetails(patientview)) {
            // match using postcode and at least 2 of address1, address2, address3
            List<GpLetter> gpLetters = gpLetterRepository.findByPostcode(patientview.getGpdetails().getGppostcode());
            Patientview.Gpdetails gp = patientview.getGpdetails();

            for (GpLetter gpLetter : gpLetters) {
                int fieldCount = 0;

                if (StringUtils.isNotEmpty(gp.getGpaddress1()) && StringUtils.isNotEmpty(gpLetter.getGpAddress1())) {
                    if (gp.getGpaddress1().equals(gpLetter.getGpAddress1())) {
                        fieldCount++;
                    }
                }

                if (StringUtils.isNotEmpty(gp.getGpaddress2()) && StringUtils.isNotEmpty(gpLetter.getGpAddress2())) {
                    if (gp.getGpaddress2().equals(gpLetter.getGpAddress2())) {
                        fieldCount++;
                    }
                }

                if (StringUtils.isNotEmpty(gp.getGpaddress3()) && StringUtils.isNotEmpty(gpLetter.getGpAddress3())) {
                    if (gp.getGpaddress3().equals(gpLetter.getGpAddress3())) {
                        fieldCount++;
                    }
                }

                if (fieldCount > 1) {
                    matchedGpLetters.add(gpLetter);
                }
            }
        }

        if (hasValidPracticeDetailsSingleMaster(patientview)) {
            // match using postcode (already checked only 1 practice with this postcode in GP master table)
            matchedGpLetters.addAll(gpLetterRepository.findByPostcode(patientview.getGpdetails().getGppostcode()));
        }

        return new ArrayList<>(matchedGpLetters);
    }
}
