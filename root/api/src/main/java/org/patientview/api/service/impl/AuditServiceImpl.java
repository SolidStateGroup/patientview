package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.AuditService;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.repository.AuditRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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

    @Override
    public Audit save(Audit audit) {
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

        return auditRepository.findAllFiltered(pageable);
    }

}
