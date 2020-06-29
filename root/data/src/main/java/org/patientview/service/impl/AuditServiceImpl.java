package org.patientview.service.impl;

import org.joda.time.DateTime;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RestrictedUsernames;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.service.AuditService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;

/**
 * Audit service, used for creating, modifying, retrieving Audits, used when the security context cannot be used (e.g.
 * Logon)
 *
 * Created by james@solidstategroup.com
 * Created on 06/08/2014
 */
@Service
public class AuditServiceImpl extends AbstractServiceImpl<AuditServiceImpl> implements AuditService {

    @Inject
    private AuditRepository auditRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private Properties properties;

    /**
     * Create an Audit item, given required properties.
     * @param auditActions An AuditActions enum representing the type of Audit
     * @param username String username
     * @param actor User who is performing the action, can be a regular User or the importer User etc
     * @param sourceObjectId ID of object being audited
     * @param sourceObjectType AuditObjectTypes type of the object being audited, e.g. Group or User
     * @param group Group, if relevant to Audit action, e.g. adding User to Group
     */
    @Override
    public void createAudit(AuditActions auditActions, String username, org.patientview.persistence.model.User actor,
                            Long sourceObjectId, AuditObjectTypes sourceObjectType, Group group) {
        Audit audit = new Audit();
        audit.setAuditActions(auditActions);
        audit.setUsername(username);

        if (actor != null) {
            audit.setActorId(actor.getId());
        }

        if (group != null) {
            Group foundGroup = groupRepository.findById(group.getId()).orElse(null);
            audit.setGroup(foundGroup);
        }

        audit.setSourceObjectId(sourceObjectId);
        if (sourceObjectType != null) {
            audit.setSourceObjectType(sourceObjectType);
        }

        save(audit);
    }

    @Override
    public void createAudit(AuditActions auditActions, String identifier, String groupCode,
                            String information, String xml, Long importerUserId) {
        Audit audit = new Audit();
        audit.setAuditActions(auditActions);
        audit.setActorId(importerUserId);
        audit.setInformation(information);
        audit.setXml(xml);

        // attempt to set identifier and user being imported from identifier
        if (identifier != null) {
            audit.setIdentifier(identifier);
            User patientUser = getUserByIdentifier(identifier);
            if (patientUser != null) {
                audit.setSourceObjectId(patientUser.getId());
                audit.setSourceObjectType(AuditObjectTypes.User);
                audit.setUsername(patientUser.getUsername());
            }
        }

        // attempt to set group doing the importing
        if (groupCode != null) {
            Group group = groupRepository.findByCode(groupCode);
            if (group != null) {
                audit.setGroup(group);
            }
        }

        save(audit);
    }
    /**
     * Remove all Audit entries associated with a User.
     * @param user User to delete Audit entries for
     */
    @Override
    public void deleteUserFromAudit(org.patientview.persistence.model.User user) {
        auditRepository.removeActorId(user.getId());
    }

    @Override
    public Long getImporterUserId() throws ResourceNotFoundException {
        User importerUser = userRepository.findByUsernameCaseInsensitive(RestrictedUsernames.IMPORTER.getName());
        if (importerUser == null) {
            throw new ResourceNotFoundException("Could not find importer user (for audit purposes)");
        }
        return importerUser.getId();
    }

    private User getUserByIdentifier(String identifier) {
        List<Identifier> identifiers = identifierRepository.findByValue(identifier);
        if (CollectionUtils.isEmpty(identifiers)) {
            return null;
        }

        // assume identifiers are unique so get the user associated with the first identifier
        return identifiers.get(0).getUser();
    }

    /**
     * Set xml column to NULL for older audit entries, configured by properties file
     */
    @Override
    public void removeOldAuditXml() {
        if (Boolean.parseBoolean(properties.getProperty("remove.old.audit.xml"))) {
            Integer days = Integer.parseInt(properties.getProperty("remove.old.audit.xml.days"));
            auditRepository.removeOldAuditXml(new DateTime().minusDays(days).toDate());
        }
    }

    /**
     * Update an existing Audit object.
     * @param audit Audit object to update
     * @return Updated Audit object
     */
    @Override
    public org.patientview.persistence.model.Audit save(org.patientview.persistence.model.Audit audit) {

        Long groupId = audit.getGroup() != null ? audit.getGroup().getId() : null;
        String sourceObjectType = audit.getSourceObjectType() != null ? audit.getSourceObjectType().getId() : null;
        String auditAction = audit.getAuditActions() != null ? audit.getAuditActions().getId() : null;
        auditRepository.save(auditAction, audit.getSourceObjectId(), sourceObjectType,
                audit.getPreValue(), audit.getPostValue(), audit.getActorId(), audit.getCreationDate(),
                audit.getIdentifier(), groupId ,audit.getInformation(), audit.getXml(), audit.getUsername());
        // return auditRepository.save(audit);
        return null;
    }
}
