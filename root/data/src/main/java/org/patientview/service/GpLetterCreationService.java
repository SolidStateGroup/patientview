package org.patientview.service;

import com.itextpdf.text.DocumentException;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 16/02/2016
 */
public interface GpLetterCreationService {

    boolean hasValidPracticeDetails(GpLetter gpLetter);

    boolean hasValidPracticeDetailsSingleMaster(GpLetter gpLetter);

    List<GpLetter> matchByGpDetails(GpLetter gpLetter);

    String generateLetter(GpLetter gpLetter, GpMaster gpMaster, String siteUrl, String outputDir)
            throws DocumentException;

    String generateSignupKey();
}