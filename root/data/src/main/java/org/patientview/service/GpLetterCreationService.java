package org.patientview.service;

import com.itextpdf.text.DocumentException;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 16/02/2016
 */
public interface GpLetterCreationService {
    String generateLetter(GpLetter gpLetter, GpMaster gpMaster, String siteUrl, String outputDir)
            throws DocumentException;
}
