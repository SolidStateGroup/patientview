package org.patientview.api.service.impl;

import com.opencsv.CSVReader;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.patientview.api.model.GpDetails;
import org.patientview.api.model.GpPatient;
import org.patientview.api.model.GpPractice;
import org.patientview.api.service.GpService;
import org.patientview.api.service.UserService;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.GpMaster;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.GpCountries;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.persistence.repository.GpMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Service
public class GpServiceImpl extends AbstractServiceImpl<GpServiceImpl> implements GpService {

    @Inject
    private GpLetterRepository gpLetterRepository;

    @Inject
    private GpMasterRepository gpMasterRepository;

    @Inject
    private UserService userService;

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

    private String getCellContent(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_NUMERIC:
                return String.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue());
            default:
                return null;
        }
    }

    private void initialise() {
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
    }

    private void updateEngland() throws IOException, ZipException {
        // get properties
        String url = properties.getProperty("gp.master.url.england");
        String filename = properties.getProperty("gp.master.filename.england");

        if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(filename)) {

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
    }

    // retrieve files from various web services to temp directory
    public Map<String, String> updateMasterTable() throws IOException, ZipException {
        initialise();
        updateEngland();
        updateScotland();
        updateNorthernIreland();

        // save objects to db
        gpMasterRepository.save(gpToSave.values());

        // output info on new/changed
        Map<String, String> status = new HashMap<>();
        status.put("total", String.valueOf(total));
        status.put("existing", String.valueOf(existingGp));
        status.put("new", String.valueOf(newGp));
        return status;
    }

    private void updateNorthernIreland() throws IOException, ZipException {
        // get properties
        String url = properties.getProperty("gp.master.url.northernireland");

        if (StringUtils.isNotEmpty(url)) {
            // download from url to temp file
            File zipFolder = new File(this.tempDirectory.concat("/" + GpCountries.NI.toString()));
            zipFolder.mkdir();
            File extractedDataFile = new File(this.tempDirectory.concat(
                    "/" + GpCountries.NI.toString() + "/" + GpCountries.NI.toString() + ".xls"));

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
            Sheet firstSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = firstSheet.iterator();
            int count = 0;

            while (iterator.hasNext()) {
                Row nextRow = iterator.next();

                if (count > 0) {
                    String practiceCode = getCellContent(nextRow.getCell(1));
                    String practiceName = getCellContent(nextRow.getCell(2));
                    String address1 = getCellContent(nextRow.getCell(3));
                    String address2 = getCellContent(nextRow.getCell(4));
                    String address3 = getCellContent(nextRow.getCell(5));
                    String postcode = getCellContent(nextRow.getCell(6));

                    // handle errors in postcode field
                    String[] postcodeSplit = postcode.split(" ");
                    if (postcodeSplit.length == 4) {
                        postcode = postcodeSplit[2] + " " + postcodeSplit[3];
                    }

                    String telephone = getCellContent(nextRow.getCell(7));

                    if (practiceCode != null) {
                        addToSaveMap(practiceCode, practiceName, address1, address2, address3, null, postcode, null,
                                telephone, GpCountries.NI);
                    }
                }
                count++;
            }

            workbook.close();
            inputStream.close();

            // archive csv file to new archive directory
            File archiveDir = new File(this.tempDirectory.concat(
                    "/archive/" + new DateTime(this.now).toString("YYYMMddhhmmss") + "/" + GpCountries.NI.toString()));
            archiveDir.mkdirs();
            FileUtils.copyFileToDirectory(extractedDataFile, archiveDir);

            // delete temp directory
            FileUtils.deleteDirectory(zipFolder);
        }
    }

    private void updateScotland() throws IOException, ZipException {
        // get properties
        String url = properties.getProperty("gp.master.url.scotland");

        if (StringUtils.isNotEmpty(url)) {
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
                    String practiceCode = getCellContent(nextRow.getCell(1));
                    String practiceName = getCellContent(nextRow.getCell(3));
                    String address1 = getCellContent(nextRow.getCell(4));
                    String address2 = getCellContent(nextRow.getCell(5));
                    String address3 = getCellContent(nextRow.getCell(6));
                    String address4 = getCellContent(nextRow.getCell(7));
                    String postcode = getCellContent(nextRow.getCell(8));
                    String telephone = getCellContent(nextRow.getCell(9));

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

    @Override
    public GpDetails validateDetails(GpDetails gpDetails) throws VerificationException {
        // check all fields present
        if (StringUtils.isEmpty(gpDetails.getForename())) {
            throw new VerificationException("forename must be set");
        }
        if (StringUtils.isEmpty(gpDetails.getSurname())) {
            throw new VerificationException("surname must be set");
        }
        if (StringUtils.isEmpty(gpDetails.getSignupKey())) {
            throw new VerificationException("signup key must be set");
        }
        if (StringUtils.isEmpty(gpDetails.getEmail())) {
            throw new VerificationException("email must be set");
        }
        if (StringUtils.isEmpty(gpDetails.getPatientIdentifier())) {
            throw new VerificationException("patient identifier must be set");
        }

        // validate no existing users with this email
        if (userService.getByEmail(gpDetails.getEmail()) != null) {
            throw new VerificationException("a user already exists with this email address");
        }

        // is an email
        if (!EmailValidator.getInstance().isValid(gpDetails.getEmail()) || !gpDetails.getEmail().contains(".")) {
            throw new VerificationException("not a valid email address");
        }

        // is a NHS email (ending with .nhs.net or .nhs.uk)
        if (!(gpDetails.getEmail().endsWith(".nhs.net") || gpDetails.getEmail().endsWith(".nhs.uk"))) {
            throw new VerificationException("not a correct NHS email address, must end with .nhs.net or .nhs.uk");
        }

        // find by signup key and nhs number, should only return one
        List<GpLetter> gpLetters = gpLetterRepository.findBySignupKeyAndIdentifier(
                gpDetails.getSignupKey(), gpDetails.getPatientIdentifier());
        if (CollectionUtils.isEmpty(gpLetters)) {
            throw new VerificationException("signup key and patient identifier either not found or do not match," +
                    " please make sure there are no spaces or unwanted characters in either");
        }

        // example...
        GpPractice gpPractice = new GpPractice();
        gpPractice.setName("Some practice");
        gpPractice.setCode("EG12345");
        gpPractice.setUrl("http://www.msn.com");
        gpDetails.getPractices().add(gpPractice);

        // example...
        GpPractice gpPractice2 = new GpPractice();
        gpPractice2.setName("Another practice");
        gpPractice2.setCode("EG00012");
        gpPractice2.setUrl("http://www.google.com");
        gpDetails.getPractices().add(gpPractice2);

        // example...
        GpPatient gpPatient = new GpPatient();
        gpPatient.setId(1L);
        gpPatient.setGpName("Dr Someone");
        gpPatient.getIdentifiers().add("1234567890");
        gpDetails.getPatients().add(gpPatient);

        // example...
        GpPatient gpPatient2 = new GpPatient();
        gpPatient2.setId(2L);
        gpPatient2.setGpName("Dr Someone");
        gpPatient2.getIdentifiers().add("986123764");
        gpDetails.getPatients().add(gpPatient2);

        return gpDetails;
    }
}
