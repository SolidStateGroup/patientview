package org.patientview.api.service.impl;

import com.opencsv.CSVReader;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.patientview.api.service.GpService;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.GpCountries;
import org.patientview.persistence.repository.GpMasterRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Service
public class GpServiceImpl extends AbstractServiceImpl<GpServiceImpl> implements GpService {

    @Inject
    private GpMasterRepository gpMasterRepository;

    @Inject
    private Properties properties;

    // retrieve files from various web services to temp directory
    public String updateMasterTable() throws IOException, ZipException {
        Date now = new Date();
        User currentUser = getCurrentUser();

        // get properties
        String tempDirectory = properties.getProperty("gp.master.temp.directory");
        String urlEngland = properties.getProperty("gp.master.url.england");
        String filenameEngland = properties.getProperty("gp.master.filename.england");

        // download from url to temp zip file
        File zipFolder = new File(tempDirectory.concat("/" + GpCountries.ENG.toString()));
        zipFolder.mkdir();
        File zipLocation = new File(tempDirectory.concat(
                "/" + GpCountries.ENG.toString() + "/" + GpCountries.ENG.toString() + ".zip"));
        FileUtils.copyURLToFile(new URL(urlEngland), zipLocation);

        // extract zip file
        ZipFile zipFile = new ZipFile(zipLocation);
        zipFile.extractAll(zipFolder.getPath());
        File extractedDataFile = new File(zipFolder.getPath().concat("/" + filenameEngland));

        // get existing and put into map to see if changed
        HashMap<String, GpMaster> existing = new HashMap<>();
        for (GpMaster gpMaster : gpMasterRepository.findAll()) {
            existing.put(gpMaster.getPracticeCode(), gpMaster);
        }

        // read CSV file line by line, extracting data to populate GpMaster objects
        int total = 0, newGp = 0, existingGp = 0;
        HashMap<String, GpMaster> gpToSave = new HashMap<>();
        CSVReader reader = new CSVReader(new FileReader(extractedDataFile));
        String[] nextLine;

        while ((nextLine = reader.readNext()) != null) {
            // retrieve data from CSV columns
            String practiceCode = nextLine[0];
            String practiceName = nextLine[1];
            String address1 = nextLine[4];
            String address2 = nextLine[5];
            String address3 = nextLine[6];
            String address4 = nextLine[7];
            String postcode = nextLine[9];
            String statusCode = nextLine[12];
            String telephone = nextLine[17];

            GpMaster gpMaster;

            // check if entry already exists for this practice code
            if (!existing.containsKey(practiceCode)) {
                // new
                gpMaster = new GpMaster();
                gpMaster.setPracticeCode(practiceCode);
                gpMaster.setCreator(currentUser);
                gpMaster.setCreated(now);
                newGp++;
            } else {
                // update
                gpMaster = existing.get(practiceCode);
                gpMaster.setLastUpdater(currentUser);
                gpMaster.setLastUpdate(now);
                existingGp++;
            }

            // set properties
            gpMaster.setPracticeName(practiceName);
            gpMaster.setCountry(GpCountries.ENG);
            gpMaster.setAddress1(StringUtils.isNotEmpty(address1) ? address1 : null);
            gpMaster.setAddress2(StringUtils.isNotEmpty(address2) ? address2 : null);
            gpMaster.setAddress3(StringUtils.isNotEmpty(address3) ? address3 : null);
            gpMaster.setAddress4(StringUtils.isNotEmpty(address4) ? address4 : null);
            gpMaster.setPostcode(StringUtils.isNotEmpty(postcode) ? postcode : null);
            gpMaster.setStatusCode(StringUtils.isNotEmpty(statusCode) ? statusCode : null);
            gpMaster.setTelephone(StringUtils.isNotEmpty(telephone) ? telephone : null);

            // add to map of GPs to save
            gpToSave.put(practiceCode, gpMaster);
            total++;
        }

        reader.close();

        // save objects to db
        gpMasterRepository.save(gpToSave.values());

        // archive csv file to new archive directory
        File archiveDir = new File(tempDirectory.concat(
                "/archive/" + new DateTime(now).toString("YYYMMddhhmmss") + "/" + GpCountries.ENG.toString()));
        archiveDir.mkdirs();
        FileUtils.copyFileToDirectory(extractedDataFile, archiveDir);

        // delete temp directory
        FileUtils.deleteDirectory(zipFolder);

        // output info on new/changed
        String status = "total: " + total + ", existing: " + existingGp + ", new: " + newGp;
        System.out.println(status);
        return status;
    }
}
