package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.Audit;
import org.patientview.api.model.AuditExternal;
import org.patientview.api.model.BaseGroup;
import org.patientview.api.model.User;
import org.patientview.api.service.ApiAuditService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ApiKey;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.enums.ApiKeyTypes;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.ApiKeyRepository;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.patientview.api.util.ApiUtil.currentUserHasRole;
import static org.patientview.api.util.ApiUtil.getCurrentUser;
import static org.patientview.api.util.ApiUtil.getCurrentUserGroupRoles;

/**
 * Audit service, used for creating, modifying, retrieving Audits, used when the security context cannot be used (e.g.
 * Logon)
 *
 * Created by james@solidstategroup.com
 * Created on 06/08/2014
 */
@Service
public class ApiAuditServiceImpl extends AbstractServiceImpl<ApiAuditServiceImpl> implements ApiAuditService {

    @Inject
    private AuditRepository auditRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ApiKeyRepository apiKeyRepository;

    @Inject
    AuditService auditService;

    /**
     * Convert a List of persistence Audit to api Audit for display in UI, adds details on source object if User or
     * Group.
     *
     * @param audits List of persistence Audit
     * @return List of api Audit
     */
    private List<Audit> convertToTransport(List<org.patientview.persistence.model.Audit> audits) {
        List<Audit> transportAudits = new ArrayList<>();

        for (org.patientview.persistence.model.Audit audit : audits) {
            Audit transportAudit = new Audit(audit);

            // get actor if exists
            if (audit.getActorId() != null) {
                org.patientview.persistence.model.User actor = userRepository.findOne(audit.getActorId());
                if (actor != null) {
                    transportAudit.setActor(new User(actor));
                }
            }

            // if source object is User get source user if exists
            if (audit.getSourceObjectType() != null && audit.getSourceObjectId() != null) {

                if (audit.getSourceObjectType().equals(AuditObjectTypes.User)) {
                    org.patientview.persistence.model.User sourceObject
                            = userRepository.findOne(audit.getSourceObjectId());

                    if (sourceObject != null) {
                        transportAudit.setSourceObjectUser(new User(sourceObject));
                    }
                } else if (audit.getSourceObjectType().equals(AuditObjectTypes.Group)) {
                    Group sourceObject
                            = groupRepository.findOne(audit.getSourceObjectId());

                    if (sourceObject != null) {
                        transportAudit.setSourceObjectGroup(new BaseGroup(sourceObject));
                    }
                }
            }

            transportAudit.setUsername(audit.getUsername());
            transportAudits.add(transportAudit);
        }

        return transportAudits;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Page<Audit> findAll(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException {

        List<Long> groupIds = convertStringArrayToLongs(getParameters.getGroupIds());

        // if specialty admin or group admin only return information relating to your groups
        if (!currentUserHasRole(RoleName.GLOBAL_ADMIN)) {
            if (groupIds.isEmpty()) {
                // haven't filtered on group, add list of user's group ids
                List<GroupRole> groupRoles = getCurrentUserGroupRoles();

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
                    if (!isUserMemberOfGroup(getCurrentUser(), entityGroup)) {
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

        Date start;
        Date end;

        if (getParameters.getStart() != null) {
            start = new Date(getParameters.getStart());
        } else {
            start = new Date(0);
        }

        if (getParameters.getEnd() != null) {
            end = new Date(getParameters.getEnd());
        } else {
            end = new Date();
        }

        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        PageRequest pageable = createPageRequest(pageConverted, sizeConverted, sortField, sortDirection);

        if (StringUtils.isEmpty(filterText)) {
            filterText = "%%";
        } else {
            filterText = "%" + filterText.trim().toUpperCase() + "%";
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
            if (!currentUserHasRole(RoleName.GLOBAL_ADMIN)) {
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

    /**
     * @inheritDoc
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public ServerResponse addExternalAudit(AuditExternal externalAudit) {

        if (StringUtils.isEmpty(externalAudit.getToken())) {
            throw new AuthenticationServiceException("token not found");
        }

        // validate api key
        List<ApiKey> apiKeys
                = apiKeyRepository.findByKeyAndType(externalAudit.getToken(), ApiKeyTypes.EXTERNAL_AUDIT);

        if (CollectionUtils.isEmpty(apiKeys)) {
            throw new AuthenticationServiceException("token not found");
        }

        Date now = new Date();

        // check not expired
        boolean validApiKey = false;
        if (!CollectionUtils.isEmpty(apiKeys)) {
            for (ApiKey apiKeyEntity : apiKeys) {
                if (apiKeyEntity.getExpiryDate() == null) {
                    validApiKey = true;
                } else if (apiKeyEntity.getExpiryDate().getTime() > now.getTime()) {
                    validApiKey = true;
                }
            }
        }

        if (!validApiKey) {
            throw new AuthenticationServiceException("token has expired");
        }

        if (org.springframework.util.StringUtils.isEmpty(externalAudit.getAuditAction())) {
            return new ServerResponse("missing audit action", null, false);
        }

        AuditActions actionType = null;

        try {
            actionType = AuditActions.valueOf(externalAudit.getAuditAction());
        } catch (Exception e) {
            return new ServerResponse("invalid audit action", null, false);
        }

        // only allowing to record these 2 actions
        if (!actionType.equals(AuditActions.PATIENT_DATA_FAIL) &&
                !actionType.equals(AuditActions.PATIENT_DATA_VALIDATE_FAIL)) {
            return new ServerResponse("not allowed audit action", null, false);
        }

        // audit
        auditService.createAudit(actionType, externalAudit.getIdentifier(),
                externalAudit.getGroupCode(), externalAudit.getInformation(), externalAudit.getXml(), null);

        return new ServerResponse(null, "done", true);

    }
}
