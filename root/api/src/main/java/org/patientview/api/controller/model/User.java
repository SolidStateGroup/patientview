package org.patientview.api.controller.model;

import org.patientview.persistence.model.Feature;

import java.util.Map;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 17/07/2014
 */
public class User {

    public Long id;
    public String password;
    public String changePassword;
    public String verificationCode;
    public String email;
    public String firstName;
    public String lastName;
    public Map<Group, Set<Role>> groups;
    public Set<Feature> features;
    public Set<String> identifiers;







}
