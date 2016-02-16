package org.patientview.api.service.impl;

import com.opencsv.CSVReader;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.patientview.api.model.GpDetails;
import org.patientview.api.model.GpPractice;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.GpService;
import org.patientview.api.service.NhsChoicesService;
import org.patientview.api.service.UserService;
import org.patientview.config.exception.VerificationException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.GpPatient;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.GpCountries;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.ContactPointTypeRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.persistence.repository.GroupFeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Service
public class GpServiceImpl extends AbstractServiceImpl<GpServiceImpl> implements GpService {

    @Inject
    private AuditRepository auditRepository;

    @Inject
    private ContactPointTypeRepository contactPointTypeRepository;

    @Inject
    private EmailService emailService;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GpLetterRepository gpLetterRepository;

    @Inject
    private GpMasterRepository gpMasterRepository;

    @Inject
    private GroupFeatureRepository groupFeatureRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupRoleRepository groupRoleRepository;

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private NhsChoicesService nhsChoicesService;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private UserFeatureRepository userFeatureRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Inject
    private Properties properties;

    private User currentUser;
    private Map<String, GpMaster> existing;
    private Map<String, GpMaster> gpToSave;
    private Date now;
    private String tempDirectory;
    private int total, newGp, existingGp;

    private void addPracticesAndPatients(GpDetails gpDetails, GpLetter gpLetter) throws VerificationException {
        // get practices from GP master table by postcode, must return at least one
        List<GpPractice> masterTablePractices = getGpPracticesFromMasterTable(gpLetter.getGpPostcode());

        // validate that practice code is not the same as any already claimed gp letter (for multi postcode)
        for (GpPractice gpPractice : masterTablePractices) {
            if (CollectionUtils.isEmpty(gpLetterRepository.findByClaimedPracticeCode(gpPractice.getCode()))) {
                gpDetails.getPractices().add(gpPractice);
            }
        }

        if (gpDetails.getPractices().isEmpty()) {
            throw new VerificationException("Could not retrieve your practice details, it may have already "
                    + "been claimed. Please <a href=\"mailto:"
                    + properties.getProperty("central.support.contact.email")
                    + "\">click here</a> to email the PatientView support desk with details of your request.");
        }

        // add patients, found via postcodes of practitioners in FHIR linked to user accounts using fhir link
        gpDetails.getPatients().addAll(fhirResource.getGpPatientsFromPostcode(gpLetter.getGpPostcode()));

        if (gpDetails.getPatients().isEmpty()) {
            throw new VerificationException("Could not retrieve any patients for your practice");
        }
    }

    private void addToSaveMap(String practiceCode, String practiceName, String address1, String address2,
                              String address3, String address4, String postcode, String statusCode, String telephone,
                              GpCountries country) {
        GpMaster gpMaster;

        // check if entry already exists for this practice code
        if (!this.existing.containsKey(practiceCode)) {
            // new
            gpMaster = new GpMaster();
            gpMaster.setPracticeCode(practiceCode);
            gpMaster.setCreator(currentUser);
            gpMaster.setCreated(this.now);
            newGp++;
        } else {
            // update
            gpMaster = this.existing.get(practiceCode);
            gpMaster.setLastUpdater(currentUser);
            gpMaster.setLastUpdate(this.now);
            existingGp++;
        }

        // set properties
        gpMaster.setPracticeName(practiceName);
        gpMaster.setCountry(country);
        gpMaster.setAddress1(StringUtils.isNotEmpty(address1) ? address1 : null);
        gpMaster.setAddress2(StringUtils.isNotEmpty(address2) ? address2 : null);
        gpMaster.setAddress3(StringUtils.isNotEmpty(address3) ? address3 : null);
        gpMaster.setAddress4(StringUtils.isNotEmpty(address4) ? address4 : null);
        gpMaster.setPostcode(StringUtils.isNotEmpty(postcode) ? postcode : null);
        gpMaster.setStatusCode(StringUtils.isNotEmpty(statusCode) ? statusCode : null);
        gpMaster.setTelephone(StringUtils.isNotEmpty(telephone) ? telephone : null);

        // add to map of GPs to save
        gpToSave.put(practiceCode, gpMaster);
        total++;
    }

    @Override
    @Transactional
    public GpDetails claim(GpDetails gpDetails) throws VerificationException {
        // validate user entered details again
        GpLetter gpLetter = validateGpDetails(gpDetails);

        // validate selected practice
        if (CollectionUtils.isEmpty(gpDetails.getPractices())) {
            throw new VerificationException("No practice selected");
        }
        if (gpDetails.getPractices().size() != 1) {
            throw new VerificationException("More than one practice selected");
        }
        if (StringUtils.isEmpty(gpDetails.getPractices().get(0).getCode())) {
            throw new VerificationException("Selected practice data incorrect");
        }

        // get details from gp master table (should only be one)
        List<GpMaster> gpMasters = gpMasterRepository.findByPracticeCode(gpDetails.getPractices().get(0).getCode());
        if (CollectionUtils.isEmpty(gpMasters)) {
            throw new VerificationException("Selected practice not found in PatientView");
        }

        GpMaster gpMaster = gpMasters.get(0);

        // validate patients
        if (CollectionUtils.isEmpty(gpDetails.getPatients())) {
            throw new VerificationException("No patients selected, please select at least one");
        }

        // check GENERAL_PRACTICE specialty exists
        Group generalPracticeSpecialty = groupRepository.findByCode(HiddenGroupCodes.GENERAL_PRACTICE.toString());
        if (generalPracticeSpecialty == null) {
            throw new VerificationException("General practice specialty does not exist");
        }

        // GP_ADMIN role used when creating group roles for new gp user
        Role gpAdminRole = roleRepository.findByRoleTypeAndName(RoleType.STAFF, RoleName.GP_ADMIN);
        if (gpAdminRole == null) {
            throw new VerificationException("Suitable admin Role does not exist");
        }

        // PATIENT role used when creating group roles for existing patient users
        Role patientRole = roleRepository.findByRoleTypeAndName(RoleType.PATIENT, RoleName.PATIENT);
        if (patientRole == null) {
            throw new VerificationException("Suitable patient Role does not exist");
        }

        // MESSAGING feature, added to group and user
        Feature messagingFeature = featureRepository.findByName(FeatureType.MESSAGING.toString());
        if (messagingFeature == null) {
            throw new VerificationException("Required MESSAGING feature not found");
        }

        if (groupRepository.findByCode(gpMaster.getPracticeCode()) != null) {
            throw new VerificationException("Group already created");
        }

        // build user
        String password = CommonUtils.generatePassword();
        User gpAdminUser = createGpAdminUser(gpDetails, password, messagingFeature);

        // build GENERAL_PRACTICE group
        Group gpGroup = createGpGroup(gpDetails, gpMaster, gpAdminUser, messagingFeature, generalPracticeSpecialty);

        // create gp admin GroupRole for newly created GENERAL_PRACTICE group
        GroupRole adminGroupRole = new GroupRole(gpAdminUser, gpGroup, gpAdminRole);
        gpAdminUser.getGroupRoles().add(adminGroupRole);

        // create gp admin GroupRole for GENERAL_PRACTICE specialty
        GroupRole adminSpecialtyGroupRole = new GroupRole(gpAdminUser, generalPracticeSpecialty, gpAdminRole);
        gpAdminUser.getGroupRoles().add(adminSpecialtyGroupRole);

        // add patients to group by creating group roles, including GENERAL_PRACTICE specialty group role if not present
        List<GroupRole> patientGroupRoles = new ArrayList<>();

        for (GpPatient gpPatient : gpDetails.getPatients()) {
            User patientUser = userRepository.findOne(gpPatient.getId());
            if (patientUser != null) {
                // check does not already have group role for GENERAL_PRACTICE specialty, if not then add
                if (groupRoleRepository.findByUserGroupRole(
                        patientUser, generalPracticeSpecialty, patientRole) == null) {
                    GroupRole specialtyGroupRole = new GroupRole(patientUser, generalPracticeSpecialty, patientRole);
                    specialtyGroupRole.setCreator(gpAdminUser);
                    patientGroupRoles.add(specialtyGroupRole);
                }

                // add new group role for newly created gp group
                GroupRole patientGroupRole = new GroupRole(patientUser, gpGroup, patientRole);
                patientGroupRole.setCreator(gpAdminUser);
                patientGroupRoles.add(patientGroupRole);
                patientUser.getGroupRoles().add(patientGroupRole);
            }
        }

        // claim all GP letters with same details
        List<GpLetter> matchedGpLetters = matchByGpDetails(gpLetter);
        Date now = new Date();
        for (GpLetter matchedGpLetter : matchedGpLetters) {
            matchedGpLetter.setClaimedEmail(gpAdminUser.getEmail());
            matchedGpLetter.setClaimedDate(now);
            matchedGpLetter.setClaimedPracticeCode(gpGroup.getCode());
            matchedGpLetter.setClaimedGroup(gpGroup);
        }

        if (matchedGpLetters.isEmpty()) {
            throw new VerificationException("Could not match details with existing practice details");
        }

        // persist
        try {
            claimPersist(gpAdminUser, gpGroup, patientGroupRoles, matchedGpLetters);
        } catch (Exception e) {
            // failed to persist
            throw new VerificationException("Error saving");
        }

        // send email to user
        sendGpAdminWelcomeEmail(gpAdminUser);

        // add created username and password
        gpDetails.setUsername(gpAdminUser.getUsername());
        gpDetails.setPassword(password);

        return gpDetails;
    }

    @Transactional
    private void claimPersist(User gpAdminUser, Group gpGroup, List<GroupRole> patientGroupRoles,
                              List<GpLetter> matchedGpLetters) throws Exception {

        userRepository.save(gpAdminUser);
        createAudit(AuditActions.ADMIN_ADD, gpAdminUser.getUsername(), gpAdminUser,
                null, gpAdminUser.getId(), AuditObjectTypes.User);

        if (!gpAdminUser.getUserFeatures().isEmpty()) {
            userFeatureRepository.save(gpAdminUser.getUserFeatures());
        }
        if (!gpAdminUser.getGroupRoles().isEmpty()) {
            groupRoleRepository.save(gpAdminUser.getGroupRoles());

            for (GroupRole groupRole : gpAdminUser.getGroupRoles()) {
                createAudit(AuditActions.ADMIN_GROUP_ROLE_ADD, gpAdminUser.getUsername(), gpAdminUser,
                        groupRole.getGroup(), gpAdminUser.getId(), AuditObjectTypes.User);
            }
        }

        groupRepository.save(gpGroup);
        createAudit(AuditActions.GROUP_ADD, null, gpAdminUser, null, gpGroup.getId(), AuditObjectTypes.Group);

        if (!gpGroup.getGroupFeatures().isEmpty()) {
            groupFeatureRepository.save(gpGroup.getGroupFeatures());
        }
        if (!patientGroupRoles.isEmpty()) {
            groupRoleRepository.save(patientGroupRoles);

            for (GroupRole groupRole : patientGroupRoles) {
                createAudit(AuditActions.PATIENT_GROUP_ROLE_ADD, groupRole.getUser().getUsername(), gpAdminUser,
                        groupRole.getGroup(), groupRole.getUser().getId(), AuditObjectTypes.User);
            }
        }
        gpLetterRepository.save(matchedGpLetters);
    }

    private void createAudit(AuditActions action, String username, User actor, Group group,
                             Long sourceObjectId, AuditObjectTypes sourceObjectType) {
        Audit audit = new Audit();
        audit.setAuditActions(action);
        audit.setUsername(username);
        audit.setActorId(actor.getId());
        audit.setSourceObjectId(sourceObjectId);
        audit.setSourceObjectType(sourceObjectType);

        if (group != null) {
            audit.setGroup(group);
        }

        auditRepository.save(audit);
    }

    private User createGpAdminUser(GpDetails gpDetails, String password, Feature messagingFeature)
            throws VerificationException {

        // DEFAULT_MESSAGING_CONTACT feature, added to user
        Feature defaultMessagingContactFeature
                = featureRepository.findByName(FeatureType.DEFAULT_MESSAGING_CONTACT.toString());
        if (defaultMessagingContactFeature == null) {
            throw new VerificationException("Required DEFAULT_MESSAGING_CONTACT feature not found");
        }

        // create new user, with basic details from user entered data
        User user = new User();
        user.setForename(gpDetails.getForename());
        user.setSurname(gpDetails.getSurname());
        user.setEmail(gpDetails.getEmail());
        user.setUsername(gpDetails.getEmail());
        user.setChangePassword(true);
        user.setLocked(false);
        user.setDummy(false);
        user.setEmailVerified(false);
        user.setDeleted(false);
        user.setGroupRoles(new HashSet<GroupRole>());
        user.setUserFeatures(new HashSet<UserFeature>());

        // add password
        try {
            String salt = CommonUtils.generateSalt();
            user.setSalt(salt);
            user.setPassword(DigestUtils.sha256Hex(password + salt));
        } catch (NoSuchAlgorithmException nsa) {
            throw new VerificationException("Could not generate password");
        }

        // add user MESSAGING feature
        UserFeature userMessagingFeature = new UserFeature(messagingFeature);
        userMessagingFeature.setCreator(user);
        userMessagingFeature.setUser(user);
        user.getUserFeatures().add(userMessagingFeature);

        // add user DEFAULT_MESSAGING_CONTACT feature
        UserFeature userDefaultMessagingContactFeature = new UserFeature(defaultMessagingContactFeature);
        userDefaultMessagingContactFeature.setCreator(user);
        userDefaultMessagingContactFeature.setUser(user);
        user.getUserFeatures().add(userDefaultMessagingContactFeature);

        user.setCreator(user);

        return user;
    }

    private Group createGpGroup(GpDetails gpDetails, GpMaster gpMaster, User user,
            Feature messagingFeature, Group generalPracticeSpecialty) throws VerificationException {

        // group basic details
        Group group = new Group();
        group.setVisible(true);
        group.setVisibleToJoin(false);
        group.setContactPoints(new HashSet<ContactPoint>());
        group.setLinks(new HashSet<Link>());
        group.setGroupFeatures(new HashSet<GroupFeature>());
        group.setGroupRelationships(new HashSet<GroupRelationship>());

        // name from GP master practice name, shortname from GP practice code with "GP-"
        group.setName(gpMaster.getPracticeName());
        group.setShortName("GP-" + gpMaster.getPracticeCode());

        // address from GP master address, combining address3 and address4 if present
        group.setAddress1(gpMaster.getAddress1());
        group.setAddress2(gpMaster.getAddress2());
        group.setCreator(user);

        if (StringUtils.isNotEmpty(gpMaster.getAddress3())) {
            group.setAddress3(gpMaster.getAddress3());
            if (StringUtils.isNotEmpty(gpMaster.getAddress4())) {
                group.setAddress3(group.getAddress3() + ", " + gpMaster.getAddress4());
            }
        }
        group.setPostcode(gpMaster.getPostcode());

        // code from GP master code
        group.setCode(gpMaster.getPracticeCode());

        // group type, set as GENERAL_PRACTICE
        group.setGroupType(
                lookupRepository.findByTypeAndValue(LookupTypes.GROUP, GroupTypes.GENERAL_PRACTICE.toString()));

        // pv admin email contact point, from user's entered email address
        ContactPoint pvAdminEmail = new ContactPoint();
        pvAdminEmail.setGroup(group);
        pvAdminEmail.setContent(gpDetails.getEmail());
        pvAdminEmail.setCreator(user);
        List<ContactPointType> pvAdminEmailType
                = contactPointTypeRepository.findByValue(ContactPointTypes.PV_ADMIN_EMAIL);
        if (!pvAdminEmailType.isEmpty()) {
            pvAdminEmail.setContactPointType(pvAdminEmailType.get(0));
            group.getContactPoints().add(pvAdminEmail);
        }

        // unit enquiries phone contact point, from GP master telephone
        if (StringUtils.isNotEmpty(gpMaster.getTelephone())) {
            ContactPoint unitEnquiriesPhone = new ContactPoint();
            unitEnquiriesPhone.setGroup(group);
            unitEnquiriesPhone.setContent(gpMaster.getTelephone());
            unitEnquiriesPhone.setCreator(user);
            List<ContactPointType> unitEnquiriesType
                    = contactPointTypeRepository.findByValue(ContactPointTypes.UNIT_ENQUIRIES_PHONE);
            if (!unitEnquiriesType.isEmpty()) {
                unitEnquiriesPhone.setContactPointType(unitEnquiriesType.get(0));
                group.getContactPoints().add(unitEnquiriesPhone);
            }
        }

        // web link, from GP master url (found from NHS choices when validating)
        if (StringUtils.isNotEmpty(gpMaster.getUrl())) {
            Link link = new Link();
            link.setGroup(group);
            link.setDisplayOrder(0);
            link.setLink(gpMaster.getUrl());
            link.setCreator(user);
            group.getLinks().add(link);
        }

        // add group MESSAGING feature
        GroupFeature groupFeature = new GroupFeature();
        groupFeature.setFeature(messagingFeature);
        groupFeature.setGroup(group);
        groupFeature.setCreator(user);
        group.getGroupFeatures().add(groupFeature);

        // set group parent to GENERAL_PRACTICE
        GroupRelationship childRelationship = new GroupRelationship();
        childRelationship.setSourceGroup(generalPracticeSpecialty);
        childRelationship.setObjectGroup(group);
        childRelationship.setRelationshipType(RelationshipTypes.CHILD);
        childRelationship.setCreator(user);
        group.getGroupRelationships().add(childRelationship);

        GroupRelationship parentRelationship = new GroupRelationship();
        parentRelationship.setSourceGroup(group);
        parentRelationship.setObjectGroup(generalPracticeSpecialty);
        parentRelationship.setRelationshipType(RelationshipTypes.PARENT);
        parentRelationship.setCreator(user);
        group.getGroupRelationships().add(parentRelationship);

        return group;
    }

    private String getCellContent(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_NUMERIC:
                return String.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue());
            default:
                return null;
        }
    }

    private List<GpPractice> getGpPracticesFromMasterTable(String gpPostcode) {
        List<GpPractice> gpPractices = new ArrayList<>();

        // search for GP practices in GP master table by postcode
        List<GpMaster> gpMasters = gpMasterRepository.findByPostcode(gpPostcode);
        for (GpMaster gpMaster : gpMasters) {
            // only add GP practices with either null or A status
            if (StringUtils.isEmpty(gpMaster.getStatusCode()) || gpMaster.getStatusCode().equals("A")) {
                GpPractice gpPractice = new GpPractice();
                gpPractice.setCode(gpMaster.getPracticeCode());
                gpPractice.setName(gpMaster.getPracticeName());

                // set the url based on retrieval from NHS choices
                if (StringUtils.isNotEmpty(gpMaster.getUrl())) {
                    // already has url
                    gpPractice.setUrl(gpMaster.getUrl());
                } else {
                    // need to update URL from NHS choices, do this here to avoid hitting API limits
                    Map<String, String> details
                            = nhsChoicesService.getDetailsByPracticeCode(gpMaster.getPracticeCode());

                    boolean updateGpMaster = false;

                    if (details != null) {
                        if (StringUtils.isNotEmpty(details.get("url"))) {
                            // url found
                            gpPractice.setUrl(details.get("url"));

                            // save updated GP master url
                            updateGpMaster = true;
                            gpMaster.setUrl(details.get("url"));
                        }
                    }

                    if (updateGpMaster) {
                        gpMasterRepository.save(gpMaster);
                    }
                }

                gpPractices.add(gpPractice);
            }
        }

        return gpPractices;
    }

    private boolean hasValidPracticeDetails(GpLetter gpLetter) {
        // check postcode is set
        if (StringUtils.isEmpty(gpLetter.getGpPostcode())) {
            return false;
        }

        // check at least one gp in master table
        if (CollectionUtils.isEmpty(gpMasterRepository.findByPostcode(gpLetter.getGpPostcode()))) {
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

    private boolean hasValidPracticeDetailsSingleMaster(GpLetter gpLetter) {
        // check postcode is set
        if (StringUtils.isEmpty(gpLetter.getGpPostcode())) {
            return false;
        }

        // validate postcode exists in GP master table and only one record
        return gpMasterRepository.findByPostcode(gpLetter.getGpPostcode()).size() == 1;
    }

    private void initialise() {
        this.now = new Date();
        this.currentUser = getCurrentUser();

        this.total = 0;
        this.newGp = 0;
        this.existingGp = 0;
        this.gpToSave = new HashMap<>();
        this.tempDirectory = properties.getProperty("gp.master.temp.directory");

        // get existing and put into map, used to see if any have changed
        this.existing = new HashMap<>();
        for (GpMaster gpMaster : gpMasterRepository.findAll()) {
            this.existing.put(gpMaster.getPracticeCode(), gpMaster);
        }
    }

    @Override
    public List<GpLetter> matchByGpDetails(GpLetter gpLetter) {
        Set<GpLetter> matchedGpLetters = new HashSet<>();

        if (hasValidPracticeDetails(gpLetter)) {
            // match using postcode and at least 2 of address1, address2, address3
            List<GpLetter> gpLetters = gpLetterRepository.findByPostcode(gpLetter.getGpPostcode());

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
            matchedGpLetters.addAll(gpLetterRepository.findByPostcode(gpLetter.getGpPostcode()));
        }

        return new ArrayList<>(matchedGpLetters);
    }

    private void sendGpAdminWelcomeEmail(User user) {
        Email email = new Email();
        email.setSenderEmail(properties.getProperty("smtp.sender.email"));
        email.setSenderName(properties.getProperty("smtp.sender.name"));
        email.setSubject("PatientView - Welcome to PatientView");
        email.setRecipients(new String[]{user.getEmail()});

        email.setBody("Dear " + user.getName() + "<br/><br/>Welcome to PatientView<br/><br/>" +
                "Your Unit Admin role has been created as requested.<br/>There is information about this role at " +
                "<a href=\"http://www.rixg.com/xxxxxxxx\">http://www.rixg.com/xxxxxxxx</a>. It is important that you " +
                "read the information there to be sure that governance and confidentiality principles are " +
                "maintained. <br/><br/>Log in at <a href=\"http://www.patientview.org\">www.patientview.org</a> " +
                "(top right) with the username below and the password generated when you claimed your account. You " +
                "will be forced to change this password when you first log in so that only you know it. " +
                "It is very important that it is difficult to guess, and kept secret." +
                "<br/><br/><strong>Username:</strong> " + user.getUsername());

        // try and send but ignore if exception and log
        try {
            emailService.sendEmail(email);
        } catch (MailException | MessagingException me) {
            LOG.error("Cannot send welcome email, continuing", me);
        }
    }

    private void updateEngland() throws IOException, ZipException {
        // get properties
        String url = properties.getProperty("gp.master.url.england");
        String filename = properties.getProperty("gp.master.filename.england");

        if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(filename)) {

            // download from url to temp zip file
            File zipFolder = new File(this.tempDirectory.concat("/" + GpCountries.ENG.toString()));
            zipFolder.mkdir();
            File zipLocation = new File(this.tempDirectory.concat(
                    "/" + GpCountries.ENG.toString() + "/" + GpCountries.ENG.toString() + ".zip"));
            FileUtils.copyURLToFile(new URL(url), zipLocation);

            // extract zip file
            ZipFile zipFile = new ZipFile(zipLocation);
            zipFile.extractAll(zipFolder.getPath());
            File extractedDataFile = new File(zipFolder.getPath().concat("/" + filename));

            // read CSV file line by line, extracting data to populate GpMaster objects
            CSVReader reader = new CSVReader(new FileReader(extractedDataFile));
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                // retrieve data from CSV columns
                String practiceCode = nextLine[0];
                String practiceName = nextLine[1];
                String address1 = nextLine[4];
                String address2 = nextLine[5];
                String address3 = nextLine[6];
                String address4 = nextLine[7];
                String postcode = nextLine[9];
                String statusCode = nextLine[12];
                String telephone = nextLine[17];

                addToSaveMap(practiceCode, practiceName, address1, address2, address3,
                        address4, postcode, statusCode, telephone, GpCountries.ENG);
            }

            reader.close();

            // archive csv file to new archive directory
            File archiveDir = new File(this.tempDirectory.concat(
                    "/archive/" + new DateTime(this.now).toString("YYYMMddhhmmss") + "/" + GpCountries.ENG.toString()));
            archiveDir.mkdirs();
            FileUtils.copyFileToDirectory(extractedDataFile, archiveDir);

            // delete temp directory
            FileUtils.deleteDirectory(zipFolder);
        }
    }

    // retrieve files from various web services to temp directory
    public Map<String, String> updateMasterTable() throws IOException, ZipException {
        initialise();
        updateEngland();
        updateScotland();
        updateNorthernIreland();

        // save objects to db
        gpMasterRepository.save(gpToSave.values());

        // output info on new/changed
        Map<String, String> status = new HashMap<>();
        status.put("total", String.valueOf(total));
        status.put("existing", String.valueOf(existingGp));
        status.put("new", String.valueOf(newGp));
        return status;
    }

    private void updateNorthernIreland() throws IOException, ZipException {
        // get properties
        String url = properties.getProperty("gp.master.url.northernireland");

        if (StringUtils.isNotEmpty(url)) {
            // download from url to temp file
            File zipFolder = new File(this.tempDirectory.concat("/" + GpCountries.NI.toString()));
            zipFolder.mkdir();
            File extractedDataFile = new File(this.tempDirectory.concat(
                    "/" + GpCountries.NI.toString() + "/" + GpCountries.NI.toString() + ".xls"));

            // needs user agent setting to avoid 403 when retrieving
            URL urlObj = new URL(url);
            URLConnection conn = urlObj.openConnection();
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
            conn.connect();
            FileUtils.copyInputStreamToFile(conn.getInputStream(), extractedDataFile);

            // read XLS file line by line, extracting data to populate GpMaster objects
            FileInputStream inputStream = new FileInputStream(new File(extractedDataFile.getAbsolutePath()));

            Workbook workbook = new HSSFWorkbook(inputStream);
            Sheet firstSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = firstSheet.iterator();
            int count = 0;

            while (iterator.hasNext()) {
                Row nextRow = iterator.next();

                if (count > 0) {
                    String practiceCode = getCellContent(nextRow.getCell(1));
                    String practiceName = getCellContent(nextRow.getCell(2));
                    String address1 = getCellContent(nextRow.getCell(3));
                    String address2 = getCellContent(nextRow.getCell(4));
                    String address3 = getCellContent(nextRow.getCell(5));
                    String postcode = getCellContent(nextRow.getCell(6));

                    // handle errors in postcode field
                    String[] postcodeSplit = postcode.split(" ");
                    if (postcodeSplit.length == 4) {
                        postcode = postcodeSplit[2] + " " + postcodeSplit[3];
                    }

                    String telephone = getCellContent(nextRow.getCell(7));

                    if (practiceCode != null) {
                        addToSaveMap(practiceCode, practiceName, address1, address2, address3, null, postcode, null,
                                telephone, GpCountries.NI);
                    }
                }
                count++;
            }

            workbook.close();
            inputStream.close();

            // archive csv file to new archive directory
            File archiveDir = new File(this.tempDirectory.concat(
                    "/archive/" + new DateTime(this.now).toString("YYYMMddhhmmss") + "/" + GpCountries.NI.toString()));
            archiveDir.mkdirs();
            FileUtils.copyFileToDirectory(extractedDataFile, archiveDir);

            // delete temp directory
            FileUtils.deleteDirectory(zipFolder);
        }
    }

    private void updateScotland() throws IOException, ZipException {
        // get properties
        String url = properties.getProperty("gp.master.url.scotland");

        if (StringUtils.isNotEmpty(url)) {
            // download from url to temp file
            File zipFolder = new File(this.tempDirectory.concat("/" + GpCountries.SCOT.toString()));
            zipFolder.mkdir();
            File extractedDataFile = new File(this.tempDirectory.concat(
                    "/" + GpCountries.SCOT.toString() + "/" + GpCountries.SCOT.toString() + ".xls"));

            // needs user agent setting to avoid 403 when retrieving
            URL urlObj = new URL(url);
            URLConnection conn = urlObj.openConnection();
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
            conn.connect();
            FileUtils.copyInputStreamToFile(conn.getInputStream(), extractedDataFile);

            // read XLS file line by line, extracting data to populate GpMaster objects
            FileInputStream inputStream = new FileInputStream(new File(extractedDataFile.getAbsolutePath()));

            Workbook workbook = new HSSFWorkbook(inputStream);
            Sheet firstSheet = workbook.getSheetAt(1);
            Iterator<Row> iterator = firstSheet.iterator();
            int count = 0;

            while (iterator.hasNext()) {
                Row nextRow = iterator.next();

                if (count > 5) {
                    String practiceCode = getCellContent(nextRow.getCell(1));
                    String practiceName = getCellContent(nextRow.getCell(3));
                    String address1 = getCellContent(nextRow.getCell(4));
                    String address2 = getCellContent(nextRow.getCell(5));
                    String address3 = getCellContent(nextRow.getCell(6));
                    String address4 = getCellContent(nextRow.getCell(7));
                    String postcode = getCellContent(nextRow.getCell(8));
                    String telephone = getCellContent(nextRow.getCell(9));

                    if (practiceCode != null) {
                        addToSaveMap(practiceCode, practiceName, address1, address2, address3, address4, postcode, null,
                                telephone, GpCountries.SCOT);
                    }
                }
                count++;
            }

            workbook.close();
            inputStream.close();

            // archive csv file to new archive directory
            File archiveDir = new File(this.tempDirectory.concat(
                "/archive/" + new DateTime(this.now).toString("YYYMMddhhmmss") + "/" + GpCountries.SCOT.toString()));
            archiveDir.mkdirs();
            FileUtils.copyFileToDirectory(extractedDataFile, archiveDir);

            // delete temp directory
            FileUtils.deleteDirectory(zipFolder);
        }
    }

    @Override
    @Transactional
    public GpDetails validateDetails(GpDetails gpDetails) throws VerificationException {
        GpLetter gpLetter = validateGpDetails(gpDetails);
        addPracticesAndPatients(gpDetails, gpLetter);

        // set central support email, for use in ui
        gpDetails.setCentralSupportEmail(properties.getProperty("central.support.contact.email"));

        return gpDetails;
    }

    private GpLetter validateGpDetails(GpDetails gpDetails) throws VerificationException {
        // check all fields present
        if (StringUtils.isEmpty(gpDetails.getForename())) {
            throw new VerificationException("Forename must be set");
        }
        if (StringUtils.isEmpty(gpDetails.getSurname())) {
            throw new VerificationException("Surname must be set");
        }
        if (StringUtils.isEmpty(gpDetails.getSignupKey())) {
            throw new VerificationException("Signup key must be set");
        }
        if (StringUtils.isEmpty(gpDetails.getEmail())) {
            throw new VerificationException("Email must be set");
        }
        if (StringUtils.isEmpty(gpDetails.getPatientIdentifier())) {
            throw new VerificationException("Patient identifier must be set");
        }

        // validate no existing users with this email
        if (userService.getByEmail(gpDetails.getEmail()) != null) {
            throw new VerificationException("A user already exists with this email address");
        }

        // validate no existing users with this email as username (as email is used for generated username)
        if (userRepository.findByUsernameCaseInsensitive(gpDetails.getEmail()) != null) {
            throw new VerificationException("A user already exists with this email address (used for username)");
        }

        // check email is a valid email string
        if (!EmailValidator.getInstance().isValid(gpDetails.getEmail()) || !gpDetails.getEmail().contains(".")) {
            throw new VerificationException("Not a valid email address");
        }

        // check email is a NHS email (ending with nhs.net, nhs.uk, hscni.net)
        if (!(gpDetails.getEmail().endsWith("nhs.net")
                || gpDetails.getEmail().endsWith("nhs.uk")
                || gpDetails.getEmail().endsWith("hscni.net"))) {
            throw new VerificationException(
                    "Not a correct NHS email address, must end with nhs.net, nhs.uk or hscni.net");
        }

        // find by signup key and nhs number (trimmed with whitespace removed), should only return one
        List<GpLetter> gpLetters = gpLetterRepository.findBySignupKeyAndIdentifier(
                gpDetails.getSignupKey().trim().replace(" ", ""),
                gpDetails.getPatientIdentifier().trim().replace(" ", ""));
        if (CollectionUtils.isEmpty(gpLetters)) {
            throw new VerificationException("Signup key and patient identifier either not found or do not match," +
                    " please make sure there are no spaces or unwanted characters in either");
        }

        // validate not already claimed
        GpLetter firstGpLetter = gpLetters.get(0);
        if (firstGpLetter.getClaimedDate() != null) {
            StringBuilder sb = new StringBuilder("Someone at your practice is already managing this group (");

            if (StringUtils.isNotEmpty(firstGpLetter.getClaimedEmail())) {
                sb.append("claimed by <a href=\"mailto:");
                sb.append(firstGpLetter.getClaimedEmail());
                sb.append("\">");
                sb.append(firstGpLetter.getClaimedEmail());
                sb.append("</a> on ");
            }

            sb.append(new SimpleDateFormat("dd-MMM-yyyy").format(firstGpLetter.getClaimedDate()));
            sb.append(")");

            if (StringUtils.isNotEmpty(firstGpLetter.getClaimedEmail())
                    && StringUtils.isNotEmpty(properties.getProperty("central.support.contact.email"))) {
                sb.append(". Please contact this person to be added as an admin user. ");
                sb.append("If you wish to query this, please <a href=\"mailto:");
                sb.append(properties.getProperty("central.support.contact.email"));
                sb.append("\">click here</a> to email the PatientView support desk with details of your request.");
            }

            throw new VerificationException(sb.toString());
        }

        return firstGpLetter;
    }
}
