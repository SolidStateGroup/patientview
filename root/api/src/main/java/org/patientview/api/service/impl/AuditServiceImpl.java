package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.AuditService;
import org.patientview.api.model.Audit;
import org.patientview.persistence.model.GetParameters;
import org.patientview.api.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
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

    @Override
    public org.patientview.persistence.model.Audit save(org.patientview.persistence.model.Audit audit) {
        return auditRepository.save(audit);
    }

    @Override
    public Page<Audit> findAll(GetParameters getParameters) {


        // TODO: security
        // check if any groupIds are not in allowed list of groups
        //if (isCurrentUserMemberOfGroup(groupRole.getGroup())) {
        //    return true;
        //}

        List<Long> groupIds = convertStringArrayToLongs(getParameters.getGroupIds());

        List<String> auditActions = new ArrayList<>();
        if (getParameters.getAuditActions() != null) {
            for (String action : getParameters.getAuditActions()) {
                for (AuditActions auditAction : AuditActions.class.getEnumConstants()) {
                    if (auditAction.getName().equals(action)) {
                        auditActions.add(auditAction.toString());
                    }
                }
            }
        }

        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        String filterText = getParameters.getFilterText();

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

        // todo group ids, identifier search

        Page<org.patientview.persistence.model.Audit> audits =  auditRepository.findAllFiltered(pageable);

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
                    && audit.getSourceObjectType().equals(AuditObjectTypes.USER.getName())
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
