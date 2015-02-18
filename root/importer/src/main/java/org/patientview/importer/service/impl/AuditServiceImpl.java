package org.patientview.importer.service.impl;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.service.AuditService;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.RestrictedUsernames;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/11/2014
 */
@Service
public class AuditServiceImpl extends AbstractServiceImpl<AuditServiceImpl> implements AuditService {

    @Inject
    private AuditRepository auditRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private GroupRepository groupRepository;

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
    
    @Override
    public void createAudit(AuditActions auditActions, String identifier, String unitCode,
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
        if (unitCode != null) {
            Group group = groupRepository.findByCode(unitCode);
            if (group != null) {
                audit.setGroup(group);
            }
        }

        auditRepository.save(audit);
    }
}
