package org.patientview.api.controller;

import org.patientview.api.service.AdminService;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.enums.RoleType;
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
public class RoleController extends BaseController<RoleController> {

    @Inject
    private AdminService adminService;

    @RequestMapping(value = "/role", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Role>> getRoles(
            @RequestParam(value = "type", required = false) String type, HttpServletRequest request) {

        if (!request.getParameterMap().containsKey("type")) {
            return new ResponseEntity<List<Role>>(adminService.getAllRoles(), HttpStatus.OK);
        }
        RoleType roleType;

        roleType = RoleType.valueOf(type);
        if (roleType == null) {
            return new ResponseEntity<List<Role>>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<List<Role>>(adminService.getRolesByType(roleType), HttpStatus.OK);
    }
}
