package org.patientview.api.service.impl;

import org.patientview.api.service.GroupRoleService;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Class to control the crud operations of Codes.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Service
public class GroupRoleServiceImpl extends AbstractServiceImpl<GroupRoleServiceImpl> implements GroupRoleService {

    @Inject
    private GroupRoleRepository groupRoleRepository;

    @Override
    public List<GroupRole> findByUser(User user) {
        return groupRoleRepository.findByUser(user);
    }
}
