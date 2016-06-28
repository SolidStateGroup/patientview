package org.patientview.service;

import com.itextpdf.text.DocumentException;
import generated.Patientview;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.enums.RoleType;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 16/02/2016
 */
public interface GpLetterService {

    void add(GpLetter gpLetter, Group sourceGroup);

    void add(Patientview patientview, Group sourceGroup);

    // public for testing only
    void addGroupRole(Long userId, Long groupId, RoleType roleType) throws ResourceNotFoundException;

    void createGpLetter(FhirLink fhirLink, Patientview patientview) throws ResourceNotFoundException;

    void createGpLetter(FhirLink fhirLink, GpLetter gpLetter) throws ResourceNotFoundException;

    String generateLetter(GpLetter gpLetter, GpMaster gpMaster, String siteUrl, String outputDir)
        throws DocumentException;

    void generateLetterPdfs();

    String generateSignupKey();

    boolean hasValidPracticeDetails(GpLetter gpLetter);

    boolean hasValidPracticeDetailsSingleMaster(GpLetter gpLetter);

    List<GpLetter> matchByGpDetails(GpLetter gpLetter);
}
