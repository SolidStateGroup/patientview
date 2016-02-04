package org.patientview.importer.service.impl;

import generated.Patientview;
import org.apache.commons.lang3.StringUtils;
import org.patientview.importer.service.GpLetterService;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GpMasterRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    @Override
    public void add(Patientview patientview) {
        Patientview.Gpdetails gp = patientview.getGpdetails();
        Patientview.Patient patient = patientview.getPatient();

        GpLetter gpLetter = new GpLetter();

        // patient details
        gpLetter.setPatientForename(patient.getPersonaldetails().getForename());
        gpLetter.setPatientSurname(patient.getPersonaldetails().getSurname());
        if (patient.getPersonaldetails().getDateofbirth() != null) {
            gpLetter.setPatientDateOfBirth(
                    patient.getPersonaldetails().getDateofbirth().toGregorianCalendar().getTime());
        }
        gpLetter.setPatientIdentifier(patient.getPersonaldetails().getNhsno());

        // gp details
        gpLetter.setGpName(gp.getGpname());
        gpLetter.setGpAddress1(gp.getGpaddress1());
        gpLetter.setGpAddress2(gp.getGpaddress2());
        gpLetter.setGpAddress3(gp.getGpaddress3());
        gpLetter.setGpAddress4(gp.getGpaddress4());
        gpLetter.setGpPostcode(gp.getGppostcode());

        // signup key (generated)
        gpLetter.setSignupKey(generateSignupKey());

        // letter (generated)
        gpLetter.setLetterContent(generateLetter());

        gpLetterRepository.save(gpLetter);
    }

    private String generateLetter() {
        return "exampleLetterBase64";
    }

    private String generateSignupKey() {
        return new BigInteger(130, new SecureRandom()).toString(32).subSequence(0, 7).toString();
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
    public boolean hasValidPracticeDetailsCheckMaster(Patientview patientview) {
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

        if (hasValidPracticeDetailsCheckMaster(patientview)) {
            // match using postcode (already checked only 1 practice with this postcode in GP master table)
            matchedGpLetters.addAll(gpLetterRepository.findByPostcode(patientview.getGpdetails().getGppostcode()));
        }

        return new ArrayList<>(matchedGpLetters);
    }
}
