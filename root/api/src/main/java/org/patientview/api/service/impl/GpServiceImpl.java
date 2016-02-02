package org.patientview.api.service.impl;

import com.opencsv.CSVReader;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.patientview.api.service.GpService;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.GpCountries;
import org.patientview.persistence.repository.GpMasterRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

    private User currentUser;
    private Map<String, GpMaster> existing;
    private Map<String, GpMaster> gpToSave;
    private Date now;
    private String tempDirectory;
    private int total, newGp, existingGp;

    private void addToSaveMap(String practiceCode, String practiceName, String address1, String address2,
                              String address3, String address4, String postcode, String statusCode, String telephone,
                              GpCountries country) {
        GpMaster gpMaster;

        // check if entry already exists for this practice code
        if (!this.existing.containsKey(practiceCode)) {
            // new
            gpMaster = new GpMaster();
            gpMaster.setPracticeCode(practiceCode);
            gpMaster.setCreator(currentUser);
            gpMaster.setCreated(this.now);
            newGp++;
        } else {
            // update
            gpMaster = this.existing.get(practiceCode);
            gpMaster.setLastUpdater(currentUser);
            gpMaster.setLastUpdate(this.now);
            existingGp++;
        }

        // set properties
        gpMaster.setPracticeName(practiceName);
        gpMaster.setCountry(country);
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

    private void updateEngland() throws IOException, ZipException {
        // get properties
        String url = properties.getProperty("gp.master.url.england");
        String filename = properties.getProperty("gp.master.filename.england");

        // download from url to temp zip file
        File zipFolder = new File(this.tempDirectory.concat("/" + GpCountries.ENG.toString()));
        zipFolder.mkdir();
        File zipLocation = new File(this.tempDirectory.concat(
                "/" + GpCountries.ENG.toString() + "/" + GpCountries.ENG.toString() + ".zip"));
        FileUtils.copyURLToFile(new URL(url), zipLocation);

        // extract zip file
        ZipFile zipFile = new ZipFile(zipLocation);
        zipFile.extractAll(zipFolder.getPath());
        File extractedDataFile = new File(zipFolder.getPath().concat("/" + filename));

        // read CSV file line by line, extracting data to populate GpMaster objects
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

            addToSaveMap(practiceCode, practiceName, address1, address2, address3,
                    address4, postcode, statusCode, telephone, GpCountries.ENG);
        }

        reader.close();

        // archive csv file to new archive directory
        File archiveDir = new File(this.tempDirectory.concat(
                "/archive/" + new DateTime(this.now).toString("YYYMMddhhmmss") + "/" + GpCountries.ENG.toString()));
        archiveDir.mkdirs();
        FileUtils.copyFileToDirectory(extractedDataFile, archiveDir);

        // delete temp directory
        FileUtils.deleteDirectory(zipFolder);
    }

    // retrieve files from various web services to temp directory
    public Map<String, String> updateMasterTable() throws IOException, ZipException {
        this.now = new Date();
        this.currentUser = getCurrentUser();

        this.total = 0;
        this.newGp = 0;
        this.existingGp = 0;
        this.gpToSave = new HashMap<>();
        this.tempDirectory = properties.getProperty("gp.master.temp.directory");

        // get existing and put into map, used to see if any have changed
        this.existing = new HashMap<>();
        for (GpMaster gpMaster : gpMasterRepository.findAll()) {
            this.existing.put(gpMaster.getPracticeCode(), gpMaster);
        }

        updateEngland();
        updateScotland();

        // save objects to db
        gpMasterRepository.save(gpToSave.values());

        // output info on new/changed
        Map<String, String> status = new HashMap<>();
        status.put("total", String.valueOf(total));
        status.put("existing", String.valueOf(existingGp));
        status.put("new", String.valueOf(newGp));
        System.out.println("total: " + total + ", existing: " + existingGp + ", new: " + newGp);
        return status;
    }

    private void updateScotland() throws IOException, ZipException {
        // get properties
        String url = properties.getProperty("gp.master.url.scotland");

        // download from url to temp file
        File zipFolder = new File(this.tempDirectory.concat("/" + GpCountries.SCOT.toString()));
        zipFolder.mkdir();
        File extractedDataFile = new File(this.tempDirectory.concat(
                "/" + GpCountries.SCOT.toString() + "/" + GpCountries.SCOT.toString() + ".xls"));

        // needs user agent setting to avoid 403 when retrieving
        URL urlObj = new URL(url);
        URLConnection conn = urlObj.openConnection();
        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
        conn.connect();
        FileUtils.copyInputStreamToFile(conn.getInputStream(), extractedDataFile);

        // read XLS file line by line, extracting data to populate GpMaster objects
        FileInputStream inputStream = new FileInputStream(new File(extractedDataFile.getAbsolutePath()));

        Workbook workbook = new HSSFWorkbook(inputStream);
        Sheet firstSheet = workbook.getSheetAt(1);
        Iterator<Row> iterator = firstSheet.iterator();
        int count = 0;

        while (iterator.hasNext()) {
            Row nextRow = iterator.next();

            if (count > 5) {
                Iterator<Cell> cellIterator = nextRow.cellIterator();
                int cellCount = 0;
                String practiceCode = null;
                String practiceName = null;
                String address1 = null;
                String address2 = null;
                String address3 = null;
                String address4 = null;
                String postcode = null;
                String telephone = null;

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    String cellContent = null;

                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_STRING:
                            cellContent = cell.getStringCellValue();
                            break;
                        case Cell.CELL_TYPE_BOOLEAN:
                            cellContent = Boolean.toString(cell.getBooleanCellValue());
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            cellContent = String.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue());
                            break;
                    }

                    if (StringUtils.isNotEmpty(cellContent)) {
                        practiceCode = cellCount == 1 ? cellContent : practiceCode;
                        practiceName = cellCount == 3 ? cellContent : practiceName;
                        address1 = cellCount == 4 ? cellContent : address1;
                        address2 = cellCount == 5 ? cellContent : address2;
                        address3 = cellCount == 6 ? cellContent : address3;
                        address4 = cellCount == 7 ? cellContent : address4;
                        postcode = cellCount == 8 ? cellContent : postcode;
                        telephone = cellCount == 9 ? cellContent : telephone;
                    }

                    cellCount++;
                }

                if (practiceCode != null) {
                    addToSaveMap(practiceCode, practiceName, address1, address2, address3, address4, postcode, null,
                            telephone, GpCountries.SCOT);
                }
            }
            count++;
        }

        workbook.close();
        inputStream.close();

        // archive csv file to new archive directory
        File archiveDir = new File(this.tempDirectory.concat(
                "/archive/" + new DateTime(this.now).toString("YYYMMddhhmmss") + "/" + GpCountries.SCOT.toString()));
        archiveDir.mkdirs();
        FileUtils.copyFileToDirectory(extractedDataFile, archiveDir);

        // delete temp directory
        FileUtils.deleteDirectory(zipFolder);
    }
}
