package org.patientview.service.impl;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.Base64;
import generated.Patientview;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.service.AuditService;
import org.patientview.service.GpLetterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
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

    private static final float POINTS_IN_CM = 28.346456692913386f;

    protected final Logger LOG = LoggerFactory.getLogger(GpLetterService.class);

    @Inject
    private AuditService auditService;

    @Inject
    private GpLetterRepository gpLetterRepository;

    @Inject
    private GpMasterRepository gpMasterRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupRoleRepository groupRoleRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private UserRepository userRepository;

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
    public void addGroupRole(Long userId, Long groupId, RoleType roleType) throws ResourceNotFoundException {
        Long importUserId = auditService.getImporterUserId();
        if (importUserId == null) {
            throw new ResourceNotFoundException("Importer user ID not found");
        }
        User importerUser = userRepository.getOne(importUserId);
        if (importerUser == null) {
            throw new ResourceNotFoundException("Importer user not found");
        }

        User user = userRepository.getOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        List<Role> roles = roleRepository.findByRoleType(roleType);
        if (CollectionUtils.isEmpty(roles)) {
            throw new ResourceNotFoundException("Role not found");
        }
        Role role = roles.get(0);

        if (groupRoleRepository.findByUserGroupRole(user, group, role) != null) {
            throw new EntityExistsException();
        }

        GroupRole groupRole = new GroupRole(user, group, role);
        groupRole.setCreator(importerUser);
        groupRoleRepository.save(groupRole);

        // add audit entry
        auditService.createAudit(AuditActions.PATIENT_GROUP_ROLE_ADD, user.getUsername(),
                importerUser, userId, AuditObjectTypes.User, group);

        // check if group being added is GENERAL_PRACTICE type
        if (group.getGroupType().getValue().equals(GroupTypes.GENERAL_PRACTICE.toString())) {
            // check General Practice specialty exists (should always be true)
            Group gpSpecialty = groupRepository.findByCode(HiddenGroupCodes.GENERAL_PRACTICE.toString());

            if (gpSpecialty != null) {
                // check if user already a member of the General Practice specialty, if not then add
                if (!groupRoleRepository.userGroupRoleExists(user.getId(), gpSpecialty.getId(), role.getId())) {
                    // not already a member, add to General Practice specialty
                    GroupRole specialtyGroupRole = new GroupRole(user, gpSpecialty, role);
                    specialtyGroupRole.setCreator(importerUser);
                    groupRoleRepository.save(specialtyGroupRole);

                    // add audit entry
                    auditService.createAudit(AuditActions.PATIENT_GROUP_ROLE_ADD, user.getUsername(),
                            importerUser, userId, AuditObjectTypes.User, gpSpecialty);
                }
            }
        }
    }

    @Override
    public void createGpLetter(FhirLink fhirLink, Patientview patientview) throws ResourceNotFoundException {
        Patientview.Gpdetails gp = patientview.getGpdetails();
        Patientview.Patient.Personaldetails personaldetails = patientview.getPatient().getPersonaldetails();

        // convert to GpLetter and check using shared service
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName(gp.getGpname());
        gpLetter.setGpAddress1(gp.getGpaddress1());
        gpLetter.setGpAddress2(gp.getGpaddress2());
        gpLetter.setGpAddress3(gp.getGpaddress3());
        gpLetter.setGpAddress4(gp.getGpaddress4());
        gpLetter.setGpPostcode(gp.getGppostcode());
        gpLetter.setPatientForename(personaldetails.getForename());
        gpLetter.setPatientSurname(personaldetails.getSurname());
        if (personaldetails.getDateofbirth() != null) {
            gpLetter.setPatientDateOfBirth(personaldetails.getDateofbirth().toGregorianCalendar().getTime());
        }
        gpLetter.setPatientIdentifier(personaldetails.getNhsno());

        createGpLetter(fhirLink, gpLetter);
    }

    @Override
    public void createGpLetter(FhirLink fhirLink, GpLetter gpLetter) throws ResourceNotFoundException {
        // verbose logging
        String identifier = gpLetter.getPatientIdentifier();
        LOG.info(identifier + ": fhirLink.isNew(): " + fhirLink.isNew());
        LOG.info(identifier + ": hasValidPracticeDetails(): " + hasValidPracticeDetails(gpLetter));
        LOG.info(identifier + ": hasValidPracticeDetailsSingleMaster(): "
                + hasValidPracticeDetailsSingleMaster(gpLetter));

        // check FhirLink is new and GP details are suitable for using in GP letter table (either enough details
        // or only have postcode but no more than one in Gp master table)
        if (fhirLink.isNew()
                && (hasValidPracticeDetails(gpLetter) || hasValidPracticeDetailsSingleMaster(gpLetter))) {
            // check if any entries exist matching GP details in GP letter table
            List<GpLetter> existingGpLetters = matchByGpDetails(gpLetter);

            // verbose logging
            LOG.info(identifier + ": gpLetters.size(): " + existingGpLetters.size());

            if (!CollectionUtils.isEmpty(existingGpLetters)) {
                // match exists, check if first entry is claimed (all will be claimed if so)
                if (existingGpLetters.get(0).getClaimedDate() != null
                        && existingGpLetters.get(0).getClaimedGroup() != null) {
                    LOG.info(identifier + " gpLetters(0) is claimed, add group role for group "
                            + existingGpLetters.get(0).getClaimedGroup().getCode());

                    // add GroupRole for this patient and GP group
                    addGroupRole(fhirLink.getUser().getId(),
                            existingGpLetters.get(0).getClaimedGroup().getId(),
                            RoleType.PATIENT);
                } else {
                    LOG.info(identifier + ": gpLetters(0) is not claimed, checking gp name is unique");

                    // entries exist but not claimed, check GP name against existing GP letter entries
                    boolean gpNameExists = false;
                    for (GpLetter existingGpLetter : existingGpLetters) {
                        if (existingGpLetter.getGpName().equals(gpLetter.getGpName())) {
                            gpNameExists = true;
                        }
                    }

                    if (!gpNameExists) {
                        LOG.info(identifier + ": gpLetters(0) is not claimed, no entry exists, create new letter");
                        // no entry for this specific GP name, create new entry
                        add(gpLetter, fhirLink.getGroup());
                    }
                }
            } else {
                LOG.info(identifier + ": gpLetters is empty, create new letter");

                // GP details do not match any in GP letter table, create new entry
                add(gpLetter, fhirLink.getGroup());
            }
        }
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
    public boolean hasValidPracticeDetailsSingleMaster(GpLetter gpLetter) {
        // check postcode is set
        if (StringUtils.isEmpty(gpLetter.getGpPostcode())) {
            return false;
        }

        // validate postcode exists in GP master table and only one record
        return gpMasterRepository.findByPostcode(gpLetter.getGpPostcode().replace(" ", "")).size() == 1;
    }

    @Override
    public String generateLetter(GpLetter gpLetter, GpMaster gpMaster, String siteUrl, String outputDir)
            throws DocumentException {

        // create new itext pdf document
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
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
        Paragraph contact = new Paragraph(new Chunk(gpLetter.getGpName()));
        contact.add(Chunk.NEWLINE);

        // GP address
        if (gpMaster == null) {
            // enough information to set address based on gp letter
            if (StringUtils.isNotEmpty(gpLetter.getGpAddress1())
                    && !gpLetter.getGpAddress1().equals(gpLetter.getGpName())) {
                contact.add(new Chunk(gpLetter.getGpAddress1()));
                contact.add(Chunk.NEWLINE);
            }
            if (StringUtils.isNotEmpty(gpLetter.getGpAddress2())) {
                contact.add(new Chunk(gpLetter.getGpAddress2()));
                contact.add(Chunk.NEWLINE);
            }
            if (StringUtils.isNotEmpty(gpLetter.getGpAddress3())) {
                contact.add(new Chunk(gpLetter.getGpAddress3()));
                contact.add(Chunk.NEWLINE);
            }
            if (StringUtils.isNotEmpty(gpLetter.getGpAddress4())) {
                contact.add(new Chunk(gpLetter.getGpAddress4()));
                contact.add(Chunk.NEWLINE);
            }
        } else {
            // not enough information for address, use gp master information instead
            if (StringUtils.isNotEmpty(gpMaster.getAddress1())) {
                contact.add(new Chunk(gpMaster.getAddress1()));
                contact.add(Chunk.NEWLINE);
            }
            if (StringUtils.isNotEmpty(gpMaster.getAddress2())) {
                contact.add(new Chunk(gpMaster.getAddress2()));
                contact.add(Chunk.NEWLINE);
            }
            if (StringUtils.isNotEmpty(gpMaster.getAddress3())) {
                contact.add(new Chunk(gpMaster.getAddress3()));
                contact.add(Chunk.NEWLINE);
            }
            if (StringUtils.isNotEmpty(gpMaster.getAddress4())) {
                contact.add(new Chunk(gpMaster.getAddress4()));
                contact.add(Chunk.NEWLINE);
            }
        }

        // GP postcode
        contact.add(new Chunk(gpLetter.getGpPostcode()));

        // position contact info for A5 window
        ColumnText ct = new ColumnText(writer.getDirectContent());
        ct.setSimpleColumn(POINTS_IN_CM * 3f, POINTS_IN_CM * 18f, POINTS_IN_CM * 21f, POINTS_IN_CM * 24.5f);
        ct.addElement(contact);
        ct.go();

        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
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
            p.add(new Chunk(" has been given access to their clinical records via PatientView ("));
            p.add(new Chunk(siteUrl.replace("http://", "").replace("https://", ""), bold));
            p.add(new Chunk("). PatientView is explained " +
                    "in more detail in the enclosed leaflet and additional information is available at "));
            p.add(new Chunk("www.rixg.org/patientview2", bold));
            p.add(new Chunk("."));
            document.add(p);
        }

        document.add(Chunk.NEWLINE);

        {
            Paragraph p = new Paragraph();
            p.add(new Chunk("According to our records "));
            p.add(new Chunk(gpLetter.getPatientForename() + " " + gpLetter.getPatientSurname()));
            p.add(new Chunk(" is registered with your practice and we would like to offer you free staff access " +
                    "to PatientView also. "));
            p.add(new Chunk("If PatientView is not yet set up for staff access in your practice, you can obtain a " +
                    "login to see the records of patients who are members by going to "));
            p.add(new Chunk(siteUrl.replace("http://", "").replace("https://", ""), bold));
            p.add(new Chunk("/gplogin", bold));
            p.add(new Chunk(" and entering the following details:"));
            document.add(p);
        }

        document.add(Chunk.NEWLINE);

        {
            Paragraph p = new Paragraph();
            p.add(new Chunk("* Your one-time signup key: "));
            p.add(new Chunk(gpLetter.getSignupKey(), bold));
            p.add(Chunk.NEWLINE);
            p.add(new Chunk("* An email address to send the login to (this must be an NHS email ending " +
                    ".nhs.net, .nhs.uk or hscni.net)."));
            p.add(Chunk.NEWLINE);
            p.add(new Chunk("* The NHS number of the patient that this letter refers to."));
            document.add(p);
        }

        document.add(Chunk.NEWLINE);

        document.add(new Paragraph(new Chunk("This will initially set you up as an Administrator for your practice, " +
                "but you can also allow access for others e.g. your practice manager. " +
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
                LOG.info(gpLetter.getPatientIdentifier() + ": Wrote GP letter to file '" + cleanPath + "'");
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
    public void generateLetterPdfs() {
        List<GpLetter> letters = gpLetterRepository.findAll();

        for (GpLetter gpLetter : letters) {
            // letter (generated)
            try {
                // if not enough information to produce letter address then use gp master
                if (!hasValidPracticeDetails(gpLetter)) {
                    List<GpMaster> gpMasters
                            = gpMasterRepository.findByPostcode(gpLetter.getGpPostcode().replace(" ", ""));

                    if (!gpMasters.isEmpty()) {
                        gpLetter.setLetterContent(generateLetter(gpLetter, gpMasters.get(0),
                                properties.getProperty("site.url"),
                                properties.getProperty("gp.letter.output.directory")));
                    } else {
                        gpLetter.setLetterContent(generateLetter(gpLetter, gpMasters.get(0),
                                properties.getProperty("site.url"),
                                properties.getProperty("gp.letter.output.directory")));
                    }
                } else {
                    gpLetter.setLetterContent(generateLetter(gpLetter, null, properties.getProperty("site.url"),
                            properties.getProperty("gp.letter.output.directory")));
                }
            } catch (DocumentException de) {
                LOG.error("Could not generate GP letter, continuing: " + de.getMessage());
            }
        }
    }

    @Override
    public String generateSignupKey() {
        return RandomStringUtils.randomAlphanumeric(7)
                .replace("o", "p").replace("0", "2").replace("1", "3").replace("l", "m").replace("i", "j")
                .toUpperCase();
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
}
