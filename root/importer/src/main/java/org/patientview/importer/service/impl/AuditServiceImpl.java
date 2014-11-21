package org.patientview.importer.service.impl;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.service.AuditService;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
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
    private GroupRepository groupRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Override
    public Audit save(Audit audit) {
        return auditRepository.save(audit);
    }

    @Override
    public Long getImporterUserId() throws ResourceNotFoundException {
        User importerUser = userRepository.findByUsernameCaseInsensitive(RestrictedUsernames.IMPORTER.getName());
        if (importerUser == null) {
            throw new ResourceNotFoundException("Could not find importer user (for audit purposes)");
        }
        return importerUser.getId();
    }

    @Override
    public User getUserByIdentifier(String identifier) {
        List<Identifier> identifiers = identifierRepository.findByValue(identifier);
        if (CollectionUtils.isEmpty(identifiers)) {
            return null;
        }

        // assume identifiers are unique so get the user associated with the first identifier
        return identifiers.get(0).getUser();
    }

    @Override
    public Group getGroupByCode(String unitCode) {
        return groupRepository.findByCode(unitCode);
    }
}
