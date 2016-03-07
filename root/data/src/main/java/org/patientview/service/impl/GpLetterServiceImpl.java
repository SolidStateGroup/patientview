package org.patientview.service.impl;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.Base64;
import generated.Patientview;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.service.GpLetterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 16/02/2016
 */
@Service
public class GpLetterServiceImpl implements GpLetterService {

    protected final Logger LOG = LoggerFactory.getLogger(GpLetterService.class);

    @Inject
    private GpLetterRepository gpLetterRepository;

    @Inject
    private GpMasterRepository gpMasterRepository;

    @Inject
    Properties properties;

    @Override
    @Transactional
    public void add(GpLetter gpLetter, Group sourceGroup) {
        gpLetter.setCreated(new Date());

        // set identifier trimmed without spaces
        gpLetter.setPatientIdentifier(gpLetter.getPatientIdentifier().trim().replace(" ", ""));

        // signup key (generated)
        gpLetter.setSignupKey(generateSignupKey());

        // source group (provided the xml, from fhirlink)
        gpLetter.setSourceGroup(sourceGroup);

        // letter (generated)
        try {
            // if not enough information to produce letter address then use gp master
            if (!hasValidPracticeDetails(gpLetter)) {
                List<GpMaster> gpMasters = gpMasterRepository.findByPostcode(gpLetter.getGpPostcode().replace(" ", ""));
                if (!gpMasters.isEmpty()) {
                    gpLetter.setLetterContent(generateLetter(gpLetter, gpMasters.get(0),
                            properties.getProperty("site.url"), properties.getProperty("gp.letter.output.directory")));
                } else {
                    gpLetter.setLetterContent(generateLetter(gpLetter, gpMasters.get(0),
                            properties.getProperty("site.url"), properties.getProperty("gp.letter.output.directory")));
                }
            } else {
                gpLetter.setLetterContent(generateLetter(gpLetter, null, properties.getProperty("site.url"),
                        properties.getProperty("gp.letter.output.directory")));
            }
        } catch (DocumentException de) {
            LOG.error("Could not generate GP letter, continuing: " + de.getMessage());
            gpLetter.setLetterContent(null);
        }

        gpLetterRepository.save(gpLetter);
    }

    @Override
    @Transactional
    public void add(Patientview patientview, Group sourceGroup) {
        // get xml gp details
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

        // set identifier trimmed without spaces
        gpLetter.setPatientIdentifier(patient.getPersonaldetails().getNhsno().trim().replace(" ", ""));

        // gp details
        gpLetter.setGpName(gp.getGpname());
        gpLetter.setGpAddress1(gp.getGpaddress1());
        gpLetter.setGpAddress2(gp.getGpaddress2());
        gpLetter.setGpAddress3(gp.getGpaddress3());
        gpLetter.setGpAddress4(gp.getGpaddress4());
        gpLetter.setGpPostcode(gp.getGppostcode());

        add(gpLetter, sourceGroup);
    }

    @Override
    public boolean hasValidPracticeDetails(GpLetter gpLetter) {
        // check postcode is set
        if (StringUtils.isEmpty(gpLetter.getGpPostcode())) {
            return false;
        }

        // check at least one gp in master table
        if (CollectionUtils.isEmpty(gpMasterRepository.findByPostcode(gpLetter.getGpPostcode().replace(" ", "")))) {
            return false;
        }

        // validate at least 2 of address1, address2, address3 is present
        int fieldCount = 0;
        if (StringUtils.isNotEmpty(gpLetter.getGpAddress1())) {
            fieldCount++;
        }
        if (StringUtils.isNotEmpty(gpLetter.getGpAddress2())) {
            fieldCount++;
        }
        if (StringUtils.isNotEmpty(gpLetter.getGpAddress3())) {
            fieldCount++;
        }

        return fieldCount > 1;
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

        return hasValidPracticeDetails(gpLetter);
    }

    @Override
    public List<GpLetter> matchByGpDetails(GpLetter gpLetter) {
        Set<GpLetter> matchedGpLetters = new HashSet<>();

        if (hasValidPracticeDetails(gpLetter)) {
            // match using postcode and at least 2 of address1, address2, address3
            // handle postcodes with and without spaces
            Set<GpLetter> gpLetters = new HashSet<>(gpLetterRepository.findByPostcode(gpLetter.getGpPostcode()));
            gpLetters.addAll(gpLetterRepository.findByPostcode(gpLetter.getGpPostcode().replace(" ", "")));

            for (GpLetter gpLetterEntity : gpLetters) {
                int fieldCount = 0;

                if (StringUtils.isNotEmpty(gpLetter.getGpAddress1())
                        && StringUtils.isNotEmpty(gpLetterEntity.getGpAddress1())) {
                    if (gpLetter.getGpAddress1().equals(gpLetterEntity.getGpAddress1())) {
                        fieldCount++;
                    }
                }

                if (StringUtils.isNotEmpty(gpLetter.getGpAddress2())
                        && StringUtils.isNotEmpty(gpLetterEntity.getGpAddress2())) {
                    if (gpLetter.getGpAddress2().equals(gpLetterEntity.getGpAddress2())) {
                        fieldCount++;
                    }
                }

                if (StringUtils.isNotEmpty(gpLetter.getGpAddress3())
                        && StringUtils.isNotEmpty(gpLetterEntity.getGpAddress3())) {
                    if (gpLetter.getGpAddress3().equals(gpLetterEntity.getGpAddress3())) {
                        fieldCount++;
                    }
                }

                if (fieldCount > 1) {
                    matchedGpLetters.add(gpLetterEntity);
                }
            }
        }

        if (hasValidPracticeDetailsSingleMaster(gpLetter)) {
            // match using postcode (already checked only 1 practice with this postcode in GP master table)
            // handle with and without spaces in postcode
            Set<GpLetter> gpLetters = new HashSet<>(gpLetterRepository.findByPostcode(gpLetter.getGpPostcode()));
            gpLetters.addAll(gpLetterRepository.findByPostcode(gpLetter.getGpPostcode().replace(" ", "")));
            matchedGpLetters.addAll(gpLetters);
        }

        return new ArrayList<>(matchedGpLetters);
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

        return matchByGpDetails(gpLetter);
    }

    @Override
    public boolean hasValidPracticeDetailsSingleMaster(GpLetter gpLetter) {
        // check postcode is set
        if (StringUtils.isEmpty(gpLetter.getGpPostcode())) {
            return false;
        }

        // validate postcode exists in GP master table and only one record
        return gpMasterRepository.findByPostcode(gpLetter.getGpPostcode().replace(" ", "")).size() == 1;
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

        return hasValidPracticeDetailsSingleMaster(gpLetter);
    }

    @Override
    public String generateLetter(GpLetter gpLetter, GpMaster gpMaster, String siteUrl, String outputDir)
            throws DocumentException {

        // create new itext pdf document
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        // add header
        Font bold = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
        Font small = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.BLACK);

        // space for printed header
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // GP name
        document.add(new Paragraph(new Chunk(gpLetter.getGpName())));

        // GP address
        if (gpMaster == null) {
            // enough information to set address based on gp letter
            if (StringUtils.isNotEmpty(gpLetter.getGpAddress1())
                    && !gpLetter.getGpAddress1().equals(gpLetter.getGpName())) {
                document.add(new Paragraph(new Chunk(gpLetter.getGpAddress1())));
            }
            if (StringUtils.isNotEmpty(gpLetter.getGpAddress2())) {
                document.add(new Paragraph(new Chunk(gpLetter.getGpAddress2())));
            }
            if (StringUtils.isNotEmpty(gpLetter.getGpAddress3())) {
                document.add(new Paragraph(new Chunk(gpLetter.getGpAddress3())));
            }
            if (StringUtils.isNotEmpty(gpLetter.getGpAddress4())) {
                document.add(new Paragraph(new Chunk(gpLetter.getGpAddress4())));
            }
        } else {
            // not enough information for address, use gp master information instead
            if (StringUtils.isNotEmpty(gpMaster.getAddress1())) {
                document.add(new Paragraph(new Chunk(gpMaster.getAddress1())));
            }
            if (StringUtils.isNotEmpty(gpMaster.getAddress2())) {
                document.add(new Paragraph(new Chunk(gpMaster.getAddress2())));
            }
            if (StringUtils.isNotEmpty(gpMaster.getAddress3())) {
                document.add(new Paragraph(new Chunk(gpMaster.getAddress3())));
            }
            if (StringUtils.isNotEmpty(gpMaster.getAddress4())) {
                document.add(new Paragraph(new Chunk(gpMaster.getAddress4())));
            }
        }

        // GP postcode
        document.add(new Paragraph(new Chunk(gpLetter.getGpPostcode())));
        document.add(Chunk.NEWLINE);

        // date
        Paragraph date = new Paragraph();
        date.add(new Chunk(new SimpleDateFormat("dd-MMM-yyyy").format(gpLetter.getCreated())));
        document.add(date);
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph(new Chunk(gpLetter.getGpName() + ",")));
        document.add(Chunk.NEWLINE);

        // add content
        {
            Paragraph p = new Paragraph();
            p.add(new Chunk("Your patient "));
            p.add(new Chunk(gpLetter.getPatientForename() + " " + gpLetter.getPatientSurname(), bold));
            if (gpLetter.getPatientDateOfBirth() != null) {
                p.add(new Chunk(" (DOB: " + new SimpleDateFormat("dd-MMM-yyyy").format(gpLetter.getPatientDateOfBirth())
                        + ")"));
            }
            p.add(new Chunk(" has been given access to their records via PatientView ("));
            p.add(new Chunk(siteUrl.replace("http://", "").replace("https://", ""), bold));
            p.add(new Chunk("). It is recorded that they are registered with your practice. PatientView is explained " +
                    "in the enclosed leaflet; more information is available at "));
            p.add(new Chunk("www.rixg.org/patientview2", bold));
            p.add(new Chunk("."));
            document.add(p);
        }

        document.add(Chunk.NEWLINE);

        {
            Paragraph p = new Paragraph();
            p.add(new Chunk("If PatientView is not yet set up for staff access in your practice, you can obtain a " +
                    "free login to see the records of patients who are members by going to "));
            p.add(new Chunk(siteUrl.replace("http://", "").replace("https://", ""), bold));
            p.add(new Chunk("/gplogin", bold));
            p.add(new Chunk(" and entering the following details:"));
            document.add(p);
        }

        document.add(Chunk.NEWLINE);

        {
            Paragraph p = new Paragraph();
            p.add(new Chunk("Your one-time signup key: "));
            p.add(new Chunk(gpLetter.getSignupKey(), bold));
            p.add(Chunk.NEWLINE);
            p.add(new Chunk("Your email address to send the login to (must be an NHS email ending" +
                    ".nhs.net, .nhs.uk or hscni.net)."));
            p.add(Chunk.NEWLINE);
            p.add(new Chunk("The NHS number of the patient that this letter refers to."));
            document.add(p);
        }

        document.add(Chunk.NEWLINE);

        document.add(new Paragraph(new Chunk("This will set you up as an Administrator for your practice, " +
                "but you can also create others (for example your practice manager could do this). " +
                "Please take a look! ")));
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph(new Chunk("Kind regards,")));
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph(new Chunk("The PatientView team")));
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        String adminEmail = ".";

        Hibernate.initialize(gpLetter.getSourceGroup().getContactPoints());

        if (!CollectionUtils.isEmpty(gpLetter.getSourceGroup().getContactPoints())) {
            for (ContactPoint contactPoint : gpLetter.getSourceGroup().getContactPoints()) {
                if (contactPoint.getContactPointType().getValue().equals(ContactPointTypes.PV_ADMIN_EMAIL)) {
                    adminEmail = " (" + contactPoint.getContent() + ").";
                }
            }
        }

        document.add(new Paragraph(new Chunk("If you think this letter has been sent incorrectly and the patient is " +
                "not in your practice, please contact the hospital’s unit administrator" + adminEmail, small)));

        // close document
        document.close();
        byte[] bytes = baos.toByteArray();

        // save document to folder with suitable filename
        if (StringUtils.isNotEmpty(outputDir)) {
            StringBuilder path = new StringBuilder(gpLetter.getGpName() + "-"
                    + gpLetter.getGpPostcode() + "-"
                    + gpLetter.getPatientForename() + "-"
                    + gpLetter.getPatientSurname() + "-");

            if (gpLetter.getPatientDateOfBirth() != null) {
                path.append(new SimpleDateFormat("dd-MMM-yyyy").format(gpLetter.getPatientDateOfBirth()));
            }

            path.append(".pdf");

            try {
                String cleanPath = outputDir + "/" +
                        path.toString().replace(" ", "_").replace("/", "_").replace("\\", "_").replace("&", "_");
                FileUtils.writeByteArrayToFile(new File(cleanPath), bytes);
                LOG.info("Wrote GP letter to file '" + cleanPath + "'");
            } catch (IOException ioe) {
                LOG.error("Could not write GP letter to file (IOException: " + ioe.getMessage() + "), continuing");
            }
        } else {
            LOG.error("Could not write GP letter to file (gp.letter.output.directory not set), continuing");
        }

        // return as base64
        return Base64.encodeBytes(bytes);
    }

    @Override
    public String generateSignupKey() {
        return RandomStringUtils.randomAlphanumeric(7)
                .replace("o", "p").replace("0", "2").replace("1", "3").replace("l", "m").replace("i", "j")
                .toUpperCase();
    }
}
