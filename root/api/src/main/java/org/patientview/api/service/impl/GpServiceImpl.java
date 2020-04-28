package org.patientview.api.service.impl;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.poi.ss.usermodel.Cell;
import org.joda.time.DateTime;
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
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.GpDetails;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.GpPatient;
import org.patientview.persistence.model.GpPractice;
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
import org.patientview.service.AuditService;
import org.patientview.service.GpLetterService;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Service
public class GpServiceImpl extends AbstractServiceImpl<GpServiceImpl> implements GpService {

    @Inject
    private AuditService auditService;

    @Inject
    private ContactPointTypeRepository contactPointTypeRepository;

    @Inject
    private EmailService emailService;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GpLetterService gpLetterService;

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
        gpMaster.setPostcode(StringUtils.isNotEmpty(postcode) ? postcode.replace(" ", "") : null);
        gpMaster.setPostcodeOriginal(StringUtils.isNotEmpty(postcode) ? postcode : null);
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
        if (gpDetails.getPractices().get(0) == null) {
            throw new VerificationException("No practice selected");
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

        // handle duplicate group roles check
        List<Long> updatedPatientIds = new ArrayList<>();

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

                // check user does not already have group role for newly created gp group
                if (!updatedPatientIds.contains(patientUser.getId())) {
                    // add new group role for newly created gp group
                    GroupRole patientGroupRole = new GroupRole(patientUser, gpGroup, patientRole);
                    patientGroupRole.setCreator(gpAdminUser);
                    patientGroupRoles.add(patientGroupRole);
                    patientUser.getGroupRoles().add(patientGroupRole);
                    updatedPatientIds.add(patientUser.getId());
                }
            }
        }

        // claim all GP letters with same details
        List<GpLetter> matchedGpLetters = gpLetterService.matchByGpDetails(gpLetter);
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
        sendGpAdminWelcomeEmail(gpAdminUser, password);

        // add created username (password is emailed)
        gpDetails.setUsername(gpAdminUser.getUsername());

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

        auditService.save(audit);
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
        List<GpMaster> gpMasters = gpMasterRepository.findByPostcode(gpPostcode.replace(" ", ""));
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
    public void invite(Long userId, FhirPatient patient) throws VerificationException {
        LOG.info("Started GP invite process");
        if (CollectionUtils.isEmpty(patient.getPractitioners())) {
            throw new VerificationException("Practitioner not set");
        }
        if (CollectionUtils.isEmpty(patient.getIdentifiers())) {
            throw new VerificationException("Identifier not set");
        }
        if (patient.getGroup() == null) {
            throw new VerificationException("Group not set");
        }

        // set source group
        Group sourceGroup = groupRepository.findOne(patient.getGroup().getId());

        // set practitioner (will only be one)
        FhirPractitioner practitioner = patient.getPractitioners().get(0);

        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpName(practitioner.getName());
        gpLetter.setGpAddress1(practitioner.getAddress1());
        gpLetter.setGpAddress2(practitioner.getAddress2());
        gpLetter.setGpAddress3(practitioner.getAddress3());
        gpLetter.setGpAddress4(practitioner.getAddress4());
        gpLetter.setGpPostcode(practitioner.getPostcode());

        gpLetter.setPatientForename(patient.getForename());
        gpLetter.setPatientSurname(patient.getSurname());
        gpLetter.setPatientDateOfBirth(patient.getDateOfBirth());

        // set patient identifier as first patient identifier
        gpLetter.setPatientIdentifier(patient.getIdentifiers().get(0).getValue());

        // same logic to check if ok as importer
        if (gpLetterService.hasValidPracticeDetails(gpLetter)
                || gpLetterService.hasValidPracticeDetailsSingleMaster(gpLetter)) {
            // check if any entries exist matching GP details in GP letter table
            List<GpLetter> existingGpLetters = gpLetterService.matchByGpDetails(gpLetter);

            // verbose logging
            LOG.info("existingGpLetters.size(): " + existingGpLetters.size());

            if (!CollectionUtils.isEmpty(existingGpLetters)) {
                // match exists, check if first entry is claimed (all will be claimed if so)
                if (existingGpLetters.get(0).getClaimedDate() == null) {
                    LOG.info("gpLetters(0) is not claimed, checking gp name is unique");

                    // entries exist but not claimed, check GP name against existing GP letter entries
                    boolean gpNameExists = false;
                    for (GpLetter existingGpLetter : existingGpLetters) {
                        if (existingGpLetter.getGpName().equals(gpLetter.getGpName())) {
                            gpNameExists = true;
                        }
                    }

                    if (!gpNameExists) {
                        LOG.info("gpLetters(0) is not claimed, no entry exists, create new letter");
                        // no entry for this specific GP name, create new entry
                        gpLetterService.add(gpLetter, sourceGroup);
                    } else {
                        throw new VerificationException("Your GP has already been invited to PatientView");
                    }
                } else {
                    throw new VerificationException("Your GP has already been invited to PatientView");
                }
            } else {
                LOG.info("gpLetters is empty, create new letter");

                // GP details do not match any in GP letter table, create new entry
                gpLetterService.add(gpLetter, sourceGroup);
            }
        } else {
            throw new VerificationException("Your GP details are incorrect, your GP cannot be invited");
        }
    }

    private void sendGpAdminWelcomeEmail(User user, String password) {
        Email email = new Email();
        email.setSenderEmail(properties.getProperty("smtp.sender.email"));
        email.setSenderName(properties.getProperty("smtp.sender.name"));
        email.setSubject("PatientView - Welcome to PatientView");
        email.setRecipients(new String[]{user.getEmail()});

        email.setBody("Dear " + user.getName() + "<br/><br/>Welcome to PatientView<br/><br/>"
                + "Your Unit Admin role has been created as requested.<br/>There is information about this role at "
                + "<a href=\"http://rixg.org/patientview2/admin-and-technical/gp-guide/\">"
                + "http://rixg.org/patientview2/admin-and-technical/gp-guide/</a>. It is important that you "
                + "read the information there to be sure that governance and confidentiality principles are "
                + "maintained. <br/><br/>Log in at <a href=\"http://www.patientview.org\">www.patientview.org</a> "
                + "(top right) with the username below and the password generated when you claimed your account. You "
                + "will be forced to change this password when you first log in so that only you know it. "
                + "It is very important that it is difficult to guess, and kept secret."
                + "<br/><br/><strong>Username:</strong> " + user.getUsername()
                + "<br/><br/><strong>Password:</strong> " + password);

        // try and send but ignore if exception and log
        try {
            emailService.sendEmail(email);
        } catch (MailException | MessagingException me) {
            LOG.error("Cannot send welcome email, continuing", me);
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

    /**
     * Updates GP list for England and Wales.
     * Downloads zip file from remote url, parses it
     *
     * @throws IOException
     * @throws ZipException
     */
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
            CSVParser parser = new CSVParser(new FileReader(extractedDataFile), CSVFormat.DEFAULT);
            for (CSVRecord record : parser) {
                String practiceCode = record.get(0);
                String practiceName = record.get(1);
                String address1 = record.get(4);
                String address2 = record.get(5);
                String address3 = record.get(6);
                String address4 = record.get(7);
                String postcode = record.get(9);
                String statusCode = record.get(12);
                String telephone = record.get(17);

                addToSaveMap(practiceCode, practiceName, address1, address2, address3,
                        address4, postcode, statusCode, telephone, GpCountries.ENG);
            }

            //close the parser
            parser.close();

            // archive csv file to new archive directory
            File archiveDir = new File(this.tempDirectory.concat(
                    "/archive/" + new DateTime(this.now).toString("YYYMMddhhmmss") + "/" + GpCountries.ENG.toString()));
            archiveDir.mkdirs();
            FileUtils.copyFileToDirectory(extractedDataFile, archiveDir);

            // delete temp directory
            FileUtils.deleteDirectory(zipFolder);
        } else {
            LOG.info("gp.master.url.england not set, continuing");
        }
    }

    private void updateNorthernIreland() throws IOException, ZipException {
        // get properties
        String url = properties.getProperty("gp.master.url.northernireland");

        if (StringUtils.isNotEmpty(url)) {
            // download from url to temp file
            File zipFolder = new File(this.tempDirectory.concat("/" + GpCountries.NI.toString()));
            zipFolder.mkdir();
            File extractedDataFile = new File(this.tempDirectory.concat(
                    "/" + GpCountries.NI.toString() + "/" + GpCountries.NI.toString() + ".csv"));

            FileUtils.copyURLToFile(new URL(url), extractedDataFile);

            // read CSV file line by line, extracting data to populate GpMaster objects
            CSVParser parser = new CSVParser(new FileReader(extractedDataFile), CSVFormat.DEFAULT);
            for (CSVRecord record : parser) {
                String practiceCode = record.get(0);
                String practiceName = record.get(1);
                String address1 = record.get(2);
                String address2 = record.get(3);
                String address3 = record.get(4);
                String postcode = record.get(5);

                if (practiceCode != null) {
                    addToSaveMap(practiceCode, practiceName, address1, address2, address3, null, postcode, null,
                            null, GpCountries.NI);
                }
            }

            //close the parser
            parser.close();

            // archive csv file to new archive directory
            File archiveDir = new File(this.tempDirectory.concat(
                    "/archive/" + new DateTime(this.now).toString("YYYMMddhhmmss") + "/" + GpCountries.NI.toString()));
            archiveDir.mkdirs();
            FileUtils.copyFileToDirectory(extractedDataFile, archiveDir);

            // delete temp directory
            FileUtils.deleteDirectory(zipFolder);
        } else {
            LOG.info("gp.master.url.northernireland not set, continuing");
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
                    "/" + GpCountries.SCOT.toString() + "/" + GpCountries.SCOT.toString() + ".csv"));

            FileUtils.copyURLToFile(new URL(url), extractedDataFile);

            // read CSV file line by line, extracting data to populate GpMaster objects
            CSVParser parser = new CSVParser(new FileReader(extractedDataFile), CSVFormat.DEFAULT);
            for (CSVRecord record : parser) {
                String practiceCode = record.get(0);
                String practiceName = record.get(1);
                String address1 = record.get(3);
                String address2 = record.get(4);
                String address3 = record.get(5);
                String address4 = record.get(6);
                String postcode = record.get(7);
                String telephone = record.get(8);

                if (practiceCode != null) {
                    addToSaveMap(practiceCode, practiceName, address1, address2, address3, address4, postcode, null,
                            telephone, GpCountries.SCOT);
                }
            }

            //close the parser
            parser.close();

            // archive csv file to new archive directory
            File archiveDir = new File(this.tempDirectory.concat(
                    "/archive/" + new DateTime(this.now).toString("YYYMMddhhmmss") + "/" + GpCountries.SCOT.toString()));
            archiveDir.mkdirs();
            FileUtils.copyFileToDirectory(extractedDataFile, archiveDir);

            // delete temp directory
            FileUtils.deleteDirectory(zipFolder);
        } else {
            LOG.info("gp.master.url.scotland not set, continuing");
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
        List<User> foundUsers = userRepository.findByEmailCaseInsensitive(gpDetails.getEmail());
        if (!CollectionUtils.isEmpty(foundUsers)) {
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
            throw new VerificationException("Signup key and patient identifier either not found or do not match,"
                    + " please make sure there are no spaces or unwanted characters in either");
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
