package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.AuditService;
import org.patientview.api.model.Audit;
import org.patientview.api.util.Util;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.api.model.User;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TODO Sprint 3 factor into aspect
 * A service to audit when the security context cannot be used (ie Logon
 *
 * Created by james@solidstategroup.com
 * Created on 06/08/2014
 */
@Service
public class AuditServiceImpl extends AbstractServiceImpl<AuditServiceImpl> implements AuditService {

    @Inject
    private AuditRepository auditRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Override
    public org.patientview.persistence.model.Audit save(org.patientview.persistence.model.Audit audit) {
        return auditRepository.save(audit);
    }

    @Override
    public Page<Audit> findAll(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException {

        List<Long> groupIds = convertStringArrayToLongs(getParameters.getGroupIds());

        // if specialty admin or group admin only return information relating to your groups
        if (!Util.doesContainRoles(RoleName.GLOBAL_ADMIN)) {
            if (groupIds.isEmpty()) {
                // haven't filtered on group, add list of user's group ids
                List<GroupRole> groupRoles = Util.getGroupRoles();

                for (GroupRole groupRole : groupRoles) {
                    if (groupRole.getRole().getName().equals(RoleName.SPECIALTY_ADMIN)) {
                        // if specialty admin add child groups (should only be any for specialty type group)
                        for (Group childGroup : groupRole.getGroup().getChildGroups()) {
                            groupIds.add(childGroup.getId());
                        }
                    } else {
                        // otherwise just add group (if not specialty)
                        if (!groupRole.getGroup().getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
                            groupIds.add(groupRole.getGroup().getId());
                        }
                    }
                }
            } else {
                // have filtered on group, check user is member of group
                for (Long groupId : groupIds) {
                    Group entityGroup = groupRepository.findOne(groupId);
                    if (entityGroup == null) {
                        throw new ResourceNotFoundException("Unknown Group");
                    }
                    if (!isCurrentUserMemberOfGroup(entityGroup)) {
                        throw new ResourceForbiddenException("Forbidden");
                    }
                }
            }
        }

        List<AuditActions> auditActions = new ArrayList<>();
        if (getParameters.getAuditActions() != null) {
            for (String action : getParameters.getAuditActions()) {
                for (AuditActions auditAction : AuditActions.class.getEnumConstants()) {
                    if (auditAction.getName().equals(action)) {
                        auditActions.add(auditAction);
                    }
                }
            }
        }

        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        String filterText = getParameters.getFilterText();
        Date start = new Date(getParameters.getStart());
        Date end = new Date(getParameters.getEnd());

        PageRequest pageable;
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        if (StringUtils.isNotEmpty(sortField) && StringUtils.isNotEmpty(sortDirection)) {
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection.equals("DESC")) {
                direction = Sort.Direction.DESC;
            }

            pageable = new PageRequest(pageConverted, sizeConverted, new Sort(new Sort.Order(direction, sortField)));
        } else {
            pageable = new PageRequest(pageConverted, sizeConverted);
        }

        if (StringUtils.isEmpty(filterText)) {
            filterText = "%%";
        } else {
            filterText = "%" + filterText.toUpperCase() + "%";
        }

        Page<org.patientview.persistence.model.Audit> audits;

        if (!groupIds.isEmpty()) {
            if (!auditActions.isEmpty()) {
                audits = auditRepository.findAllBySourceGroupAndActionFiltered(
                        start, end, filterText, groupIds, auditActions, pageable);
            } else {
                audits = auditRepository.findAllBySourceGroupFiltered(start, end, filterText, groupIds, pageable);
            }
        } else {
            // include final check to see if global admin as others should have group ids
            if (!Util.doesContainRoles(RoleName.GLOBAL_ADMIN)) {
                throw new ResourceForbiddenException("Forbidden");
            }

            if (!auditActions.isEmpty()) {
                audits = auditRepository.findAllByActionFiltered(start, end, filterText, auditActions, pageable);
            } else {
                audits = auditRepository.findAllFiltered(start, end, filterText, pageable);
            }
        }

        // convert to transport objects, create Page and return
        List<Audit> transportContent = convertToTransport(audits.getContent());
        return new PageImpl<>(transportContent, pageable, audits.getTotalElements());
    }

    private List<Audit> convertToTransport(List<org.patientview.persistence.model.Audit> audits) {
        List<Audit> transportAudits = new ArrayList<>();

        for (org.patientview.persistence.model.Audit audit : audits) {
            Audit transportAudit = new Audit(audit);

            // get actor if exists
            if (audit.getActorId() != null) {
                org.patientview.persistence.model.User actor = userRepository.findOne(audit.getActorId());
                if (actor != null) {
                    transportAudit.setActor(new User(actor, null));
                }
            }

            // if source object is User get source user if exists
            if (audit.getSourceObjectType() != null
                    && audit.getSourceObjectType().equals(AuditObjectTypes.User)
                    && audit.getSourceObjectId() != null) {

                org.patientview.persistence.model.User sourceObjectUser
                        = userRepository.findOne(audit.getSourceObjectId());

                if (sourceObjectUser != null) {
                    transportAudit.setSourceObjectUser(new User(sourceObjectUser, null));
                }
            }

            transportAudits.add(transportAudit);
        }

        return transportAudits;
    }

}
