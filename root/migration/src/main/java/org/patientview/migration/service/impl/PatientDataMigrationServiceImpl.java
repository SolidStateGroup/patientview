package org.patientview.migration.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.patientview.User;
import org.patientview.migration.util.FhirUtil;
import org.patientview.migration.util.JsonUtil;
import org.patientview.model.Patient;
import org.patientview.patientview.model.UserMapping;
import org.patientview.repository.PatientDao;
import org.patientview.repository.UserDao;
import org.patientview.repository.UserMappingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 12/06/2014
 */
public class PatientDataMigrationServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(PatientDataMigrationServiceImpl.class);

    @Inject
    private UserMappingDao userMappingDao;

    @Inject
    private UserDao userDao;

    @Inject
    private PatientDao patientDao;


    @Test
    public void migrate() {

        for (org.patientview.patientview.model.User oldUser : userDao.getAll()) {

            List<UserMapping> userMappings = userMappingDao.getAll(oldUser.getUsername());

            String url = JsonUtil.pvUrl + "/user?username=" + oldUser.getUsername();
            User newUser = JsonUtil.jsonRequest(url, User.class, null, HttpGet.class);
            Patient oldPatient = getPatient(oldUser);
            org.hl7.fhir.instance.model.Patient newPatient =  FhirUtil.getFhirPatient(oldPatient);

            try {
                String uuid = JsonUtil.getResourceUuid(JsonUtil.serializeResource(newPatient));
                newUser.setFhirResourceId(UUID.fromString(uuid));
            } catch (Exception e) {
                LOG.error("Failed to export patient {} ", oldPatient.getNhsno());
                e.printStackTrace();
                continue;
            }

            newUser = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/user", User.class, newUser, HttpPut.class);

            if (newUser == null) {
                LOG.error("Failed to update user with uuid");
            } else {
                LOG.info("Successfully created patient");
            }

        }



    }


    private Patient getPatient(org.patientview.patientview.model.User oldUser) {

        String nhsNumber = null;
        Patient patient = null;
        List<UserMapping> userMappings = userMappingDao.getAll(oldUser.getUsername());

        for (UserMapping userMapping : userMappings) {
            if (StringUtils.isNotEmpty(userMapping.getNhsno())) {
                nhsNumber = userMapping.getNhsno();
            }
        }

        if (StringUtils.isEmpty(nhsNumber)) {
            throw new IllegalStateException("This is not a patient");
        }

        List<Patient> patients = patientDao.get(nhsNumber);

        for (Patient tempPatient : patients) {
            if (!tempPatient.isLinked() && tempPatient.getSourceType().equalsIgnoreCase("patient")
                    && StringUtils.isNotEmpty(tempPatient.getSurname())) {
                return patient;
            }
        }

        throw new IllegalStateException("No suitable patient record");

    }

    private HttpResponse gsonPost(String json) throws Exception {

        HttpClient httpClient = new DefaultHttpClient();

        String postUrl="http://localhost:8082/resource";// put in your url
        HttpPost post = new HttpPost(postUrl);
        StringEntity postingString = new StringEntity(json);

        post.setEntity(postingString);
        post.setHeader("Content-type", "application/json");
        return httpClient.execute(post);

    }


}
