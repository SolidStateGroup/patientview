package org.patientview.api.controller;

import org.patientview.api.service.AdminService;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.enums.RoleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 06/06/2014
 */
@RestController
public class RoleController extends BaseController {
    private final static Logger LOG = LoggerFactory.getLogger(GroupController.class);

    @Inject
    private AdminService adminService;

    @RequestMapping(value = "/role", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Role>> getRoles(
            @RequestParam(value = "type", required = false) String type, HttpServletRequest request) {

        if (!request.getParameterMap().containsKey("type")) {
            return new ResponseEntity<List<Role>>(adminService.getAllRoles(), HttpStatus.OK);
        }
        RoleTypes roleTypes;

        roleTypes = RoleTypes.valueOf(type);
        if (roleTypes == null) {
            return new ResponseEntity<List<Role>>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<List<Role>>(adminService.getRolesByType(roleTypes), HttpStatus.OK);
    }
}
