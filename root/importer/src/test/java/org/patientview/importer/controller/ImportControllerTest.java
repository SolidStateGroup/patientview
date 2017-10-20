package org.patientview.importer.controller;

import generated.Patientview;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.importer.service.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
public class ImportControllerTest {

    @Mock
    UriComponentsBuilder uriComponentsBuilder;

    @Mock
    QueueService queueService;

    @InjectMocks
    ImportController importController;

    MockMvc mockMvc;
    
    private static final Logger LOG = LoggerFactory.getLogger(ImportControllerTest.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(importController, uriComponentsBuilder).build();
    }

    // local clear survey & response
    /*
    DELETE FROM pv_question_answer WHERE id > 4944340;
    DELETE FROM pv_question_option WHERE id > 4944340;
    DELETE FROM pv_question WHERE id > 4957410;
    DELETE FROM pv_question_group WHERE id > 4957309;
    DELETE FROM pv_survey_response_score WHERE id > 4996932;
    DELETE FROM pv_survey_response WHERE id > 4957512;
    DELETE FROM pv_survey_feedback WHERE id > 4964549;
    DELETE FROM pv_survey WHERE id > 4957408;
    */

    // staging clear survey & response
    /*
    DELETE FROM pv_question_answer WHERE id > 14883223;
    DELETE FROM pv_question_option WHERE id > 14883121;
    DELETE FROM pv_question WHERE id > 14883119;
    DELETE FROM pv_question_group WHERE id > 14883119;
    DELETE FROM pv_survey_response WHERE id > 14883222;
    DELETE FROM pv_survey_feedback WHERE id > 14890074;
    DELETE FROM pv_survey WHERE id > 14883118;
    */

    @Test
    @Ignore
    public void testBulkSurveyDescriptionImport() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/survey/survey_PROM_2.xml");
        files.add("data/xml/survey/survey_PAM_1.xml");
        files.add("data/xml/survey/survey_EQ5D_2.xml");

        for (String fileName : files) {
            URL xmlPath = Thread.currentThread().getContextClassLoader().getResource(fileName);
            File file = new File(xmlPath.toURI());
            String content = new String(Files.readAllBytes(Paths.get(file.getPath())));

            org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
            String postUrl="http://localhost:8081/importer/import/survey";
            //String postUrl = "https://test.patientview.org/importer/import/survey";
            //String postUrl = "https://production.patientview.org/importer/import/survey";

            HttpPost post = new HttpPost(postUrl);
            StringEntity postingString = new StringEntity(content);
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/xml");
            HttpResponse httpResponse = httpClient.execute(post);
            LOG.info(httpResponse.toString());
        }
    }

    @Test
    @Ignore
    public void testBulkSurveyResponseImport() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/survey_response/survey_response_PROM_1.xml");
        files.add("data/xml/survey_response/survey_response_PROM_2.xml");
        files.add("data/xml/survey_response/survey_response_PROM_3.xml");
        files.add("data/xml/survey_response/survey_response_PROM_4.xml");
        files.add("data/xml/survey_response/survey_response_PAM_1.xml");
        files.add("data/xml/survey_response/survey_response_PAM_2.xml");
        files.add("data/xml/survey_response/survey_response_PAM_3.xml");
        files.add("data/xml/survey_response/survey_response_PAM_4.xml");
        files.add("data/xml/survey_response/survey_response_EQ5D_1.xml");
        files.add("data/xml/survey_response/survey_response_EQ5D_2.xml");
        files.add("data/xml/survey_response/survey_response_EQ5D_3.xml");
        files.add("data/xml/survey_response/survey_response_EQ5D_4.xml");
        /*files.add("data/xml/survey_response/4354569111/survey_response_PROM_1.xml");
        files.add("data/xml/survey_response/4354569111/survey_response_PROM_2.xml");
        files.add("data/xml/survey_response/4354569111/survey_response_PROM_3.xml");
        files.add("data/xml/survey_response/4354569111/survey_response_PROM_4.xml");
        files.add("data/xml/survey_response/4354569111/survey_response_PAM_1.xml");
        files.add("data/xml/survey_response/4354569111/survey_response_PAM_2.xml");
        files.add("data/xml/survey_response/4354569111/survey_response_PAM_3.xml");
        files.add("data/xml/survey_response/4354569111/survey_response_PAM_4.xml");
        files.add("data/xml/survey_response/4354569111/survey_response_EQ5D_1.xml");
        files.add("data/xml/survey_response/4354569111/survey_response_EQ5D_2.xml");
        files.add("data/xml/survey_response/4354569111/survey_response_EQ5D_3.xml");
        files.add("data/xml/survey_response/4354569111/survey_response_EQ5D_4.xml");*/

        for (String fileName : files) {
            URL xmlPath = Thread.currentThread().getContextClassLoader().getResource(fileName);
            File file = new File(xmlPath.toURI());
            String content = new String(Files.readAllBytes(Paths.get(file.getPath())));

            org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
            String postUrl="http://localhost:8081/importer/import/surveyresponse";
            //String postUrl = "https://test.patientview.org/importer/import/surveyresponse";

            HttpPost post = new HttpPost(postUrl);
            StringEntity postingString = new StringEntity(content);
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/xml");
            HttpResponse httpResponse = httpClient.execute(post);
            LOG.info(httpResponse.toString());
        }
    }

    @Test
    @Ignore
    public void testBulkUkrdcImport() throws Exception {
        List<String> files = new ArrayList<>();
        /*files.add("data/xml/ukrdc/ukrdc_survey_response_PAM_1.xml");
        files.add("data/xml/ukrdc/ukrdc_survey_response_PAM_2.xml");
        files.add("data/xml/ukrdc/ukrdc_survey_response_PAM_3.xml");
        files.add("data/xml/ukrdc/ukrdc_survey_response_PAM_4.xml");*/

        /*files.add("data/xml/ukrdc/4354569111/ukrdc_survey_response_PAM_1.xml");
        files.add("data/xml/ukrdc/4354569111/ukrdc_survey_response_PAM_2.xml");
        files.add("data/xml/ukrdc/4354569111/ukrdc_survey_response_PAM_3.xml");
        files.add("data/xml/ukrdc/4354569111/ukrdc_survey_response_PAM_4.xml");*/

        /*files.add("data/xml/ukrdc/ukrdc_survey_response_PAM_3.xml");
        files.add("data/xml/ukrdc/ukrdc_survey_response_PAM_4_document.xml");*/

        //files.add("data/xml/ukrdc/4354569111/ukrdc_survey_response_PAM_4_document.xml");

        files.add("data/xml/ukrdc/ukrdc_survey_response_PAM_4_no_patient.xml");

        for (String fileName : files) {
            URL xmlPath = Thread.currentThread().getContextClassLoader().getResource(fileName);
            File file = new File(xmlPath.toURI());
            String content = new String(Files.readAllBytes(Paths.get(file.getPath())));

            org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
            //String postUrl="http://localhost:8081/importer/import/ukrdc";
            String postUrl = "https://test.patientview.org/importer/import/ukrdc";

            HttpPost post = new HttpPost(postUrl);
            StringEntity postingString = new StringEntity(content);
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/xml");
            HttpResponse httpResponse = httpClient.execute(post);
            LOG.info(httpResponse.toString());
        }
    }

    /**
     * Test: Passing an xml in full to the import resource and see if the resource can pass it to the service
     * Fail: The resource is unable to call the resource
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void testFileImport() throws Exception {
        String content = getTestFile();
        assertTrue("The test file is not null", !StringUtils.isEmpty(content));

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/import")
                    .contentType(MediaType.APPLICATION_XML)
                    .content(content))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }
        catch (Exception e) {
            fail("The post request all should not fail " + e.getCause());
        }

        Mockito.verify(queueService, Mockito.times(1)).importRecord(Mockito.any(Patientview.class));
    }

    // used to test the service once running
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTest() throws Exception {
        LOG.info(post(getTestFile()).toString());
    }

    // used to test the service once running (CKD files)
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD01() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD/RQHC7_01_1234567345.xml");
        files.add("data/xml/CKD/RQHC7_01_4325437666.xml");
        files.add("data/xml/CKD/RQHC7_01_4444443333.xml");
        files.add("data/xml/CKD/RQHC7_01_5432134678.xml");
        files.add("data/xml/CKD/RQHC7_01_5566778833.xml");
        files.add("data/xml/CKD/RQHC7_01_9879879876.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD02() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD/RQHC7_02_4325437666.xml");
        files.add("data/xml/CKD/RQHC7_02_9879879876.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD03() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD/RQHC7_03_4325437666.xml");
        files.add("data/xml/CKD/RQHC7_03_9879879876.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD04() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD/RQHC7_04_4325437666.xml");
        files.add("data/xml/CKD/RQHC7_04_9879879876.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD_2_01() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD_2/RQHC7_01_1234567345.xml");
        files.add("data/xml/CKD_2/RQHC7_01_5432134678.xml");
        files.add("data/xml/CKD_2/RQHC7_01_5566778833.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD_2_02() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD_2/RQHC7_02_1234567345.xml");
        files.add("data/xml/CKD_2/RQHC7_02_5432134678.xml");
        files.add("data/xml/CKD_2/RQHC7_02_5566778833.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD_3_01() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD_3/RQR00_01_667788990.xml");
        files.add("data/xml/CKD_3/RQR00_01_5556667778.xml");
        files.add("data/xml/CKD_3/RQR00_01_6655443322.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD_4_01() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD_4/RQR00_01_5312335683.xml");
        files.add("data/xml/CKD_4/RQR00_01_6655443322.xml");
        files.add("data/xml/CKD_4/RQR00_01_7531245791.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD_5_01() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD_5/RDDH0_01_3333445667.xml");
        files.add("data/xml/CKD_5/RDDH0_01_8888777799.xml");
        files.add("data/xml/CKD_5/RDDH0_01_9574444433.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD_6_01() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD_6/SNC01_01_3334254544.xml");
        files.add("data/xml/CKD_6/SNC01_01_6665262218.xml");
        files.add("data/xml/CKD_6/SNC01_01_7464644329.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD_7_01() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD_7/RAL01_01_8217326543.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestCKD_8() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/CKD_8/RAL01_01_8217326544.xml");
        files.add("data/xml/CKD_8/RAL01_01_8217326545.xml");
        files.add("data/xml/CKD_8/RAL01_01_8217326546.xml");
        files.add("data/xml/CKD_8/RAL01_01_8217326547.xml");
        files.add("data/xml/CKD_8/RAL01_01_8217326548.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    // hfdemo
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestHfdemo() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/hfdemo/EDINHF1_5745634581_01.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    // used to test the service once running (milestone 4 files)
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestMilestone4() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/milestone4/SAC02_01439_41737438900.xml"); // 336 observations
        files.add("data/xml/milestone4/SGC04_01436_57703939407.xml"); // big
        files.add("data/xml/milestone4/SGC04_01436_64098149107.xml"); // big, 6372 observations
        files.add("data/xml/milestone4/SGC04_01456_12314191702.xml");
        files.add("data/xml/milestone4/SGC04_01459_14018849809.xml");
        files.add("data/xml/milestone4/SGC04_01459_28039602801.xml");
        files.add("data/xml/milestone4/SGC04_01459_74569958609.xml");

        // 46 seconds local 10/11/14 fhir_delete and native create observations
        // 5 seconds local 11/11/14 native delete and create observations

        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    // used to test the service once running (2.0.6 files)
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTest206() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/2.0.6tests/SAC02_01439_1202913202.xml"); // ok, 290 observations
        files.add("data/xml/2.0.6tests/SAC02_01439_1312455233.xml"); // 400 bad request, wrong xml
        files.add("data/xml/2.0.6tests/SAC02_01439_3103833318.xml"); // ok, 336 observations
        files.add("data/xml/2.0.6tests/SGC02_01439_1312045485.xml"); // ok, 288 observations
        files.add("data/xml/2.0.6tests/SGC02_01439_2609995652.xml"); // ok, 282 observations

        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    // used to test the service once running (IBD files)
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestIbd() throws Exception {
        List<String> files = new ArrayList<>();
        // contain real data, must be added to folder manually
        files.add("data/xml/ibd/SALIBD_3466151139.xml");
        //files.add("data/xml/ibd/SALIBD_4426012465.xml");
        //files.add("data/xml/ibd/SALIBD_4500666672.xml");
        //files.add("data/xml/ibd/SALIBD_6186660419.xml");
        //files.add("data/xml/ibd/SALIBD_6304291914.xml");

        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    // IBD importer testing
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestIBDtest() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("data/xml/IBD_test/import-ibdtest.xml");
        files.add("data/xml/IBD_test/importfail1.xml");
        files.add("data/xml/IBD_test/importfail2.xml");
        files.add("data/xml/IBD_test/importfail3.xml");
        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    // GP logins, letter generation testing, db needs clearing manually after each test
    /*
        DELETE FROM pv_contact_point WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_user_group_role WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_group_relationship WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_feature_group WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_feature_user WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_fhir_link WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_gp_letter WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_audit WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_group_statistic WHERE id > 4731630;
        DELETE FROM pv_link WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_group WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_user_token WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_message_read_receipt WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_message WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_conversation_user_label WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_conversation_user WHERE creation_date > '2016-02-10 00:00:00.000';
        DELETE FROM pv_user WHERE creation_date > '2016-02-10 00:00:00.000';
    */
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestGpLogins_local() throws Exception {
        // all data, single match in gp master - creates letter
        //post(getFileFromString("data/xml/gplogin/RENALB_1111111111_GU47_0UB.xml"));

        // all data, multi match in gp master - creates letter, adds test results
        post(getFileFromString("data/xml/gplogin/RENALB_1111111111_B11_4AN.xml"));

        // only name and postcode, single match in gp master for postcode - creates letter
        //post(getFileFromString("data/xml/gplogin/RENALB_1111111111_GU47_0UB_name_postcode.xml"));

        // only name and postcode, multiple matches (2) in gp master for postcode - no letter
        //post(getFileFromString("data/xml/gplogin/RENALB_1111111111_ME20_7SE_name_postcode_duplicate_master.xml"));

        // two blank addresses, single match in gp master - creates letter
        //post(getFileFromString("data/xml/gplogin/RENALB_1111111111_GU47_0UB_two_blank_address.xml"));

        // no postcode, no match in gp master - no letter
        //post(getFileFromString("data/xml/gplogin/RENALB_1111111111_GU47_0UB_no_postcode.xml"));

        // wrong postcode, no match in gp master - no letter
        //post(getFileFromString("data/xml/gplogin/RENALB_1111111111_AB12_3CD.xml"));

        // bad gp name, should rename file when saving
        //post(getFileFromString("data/xml/gplogin/RENALB_1111111111_GU47_0UB_bad_name_postcode.xml"));
    }

    // GP logins on staging
    @Test
    @Ignore("IntegrationTest")
    public void importIntegrationTestGpLogins_staging() throws Exception {
        //post(getFileFromString("data/xml/gplogin_test/p1.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p1-2.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p1-3.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p2.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p2-2.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p2-3.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p11.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p2-4.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p2-5.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p2-6.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p3.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p3-2.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p3-3.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p4.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p4-2.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p4-3.xml"));
        //post(getFileFromString("data/xml/gplogin_test/p2-5.xml"));

        // second batch
        //post(getFileFromString("data/xml/gplogin_test_2/p2-5.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p3-3.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p4-2.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p4-3.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p6.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p6-2.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p6-3.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p6-4.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p6-3-2.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p7.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p8.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p9.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p9-2.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p9-3.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p9-4.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p10.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p10-2.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p10-3.xml"));
        //post(getFileFromString("data/xml/gplogin_test_2/p7-2.xml"));

        post(getFileFromString("data/xml/gplogin_test_2/vvvv.xml"));
    }


    // used to test mobile alerts notification for patient
    @Test
    @Ignore("IntegrationTest")
    public void testImportResults_AlertNotification() throws Exception {
        List<String> files = new ArrayList<>();
        // contain real data, must be added to folder manually
        files.add("data/xml/alerts/alerts_test.xml");

        for (String file : files) {
            post(getFileFromString(file));
        }
    }

    String getTestFile() throws IOException, URISyntaxException {
        // local testing
        String fileName = "data/xml/SAC02_01436_1111111111.xml";
        //String fileName = "data/xml/EDINHF1_01436_1111111111.xml";
        //String fileName = "data/xml/SALIBD_01436_1111111111.xml";
        //String fileName = "data/xml/2.0.6tests/SAC02_01436_1111111111_PDF.xml";
        //String fileName = "data/xml/ibdtest1_1.xml";

        // IBD
        //String fileName = "data/xml/ibd/1111111111_ibd.xml";

        // partial migration
        //String fileName = "data/xml/partialmigration/test1.xml";

        // survey testing
        //String fileName = "data/xml/surveytestuser.xml";

        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource(fileName);
        File file = new File(xmlPath.toURI());
        return new String(Files.readAllBytes(Paths.get(file.getPath())));
    }

    String getFileFromString(String fileLocation) throws IOException, URISyntaxException {
        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource(fileLocation);
        File file = new File(xmlPath.toURI());
        return new String(Files.readAllBytes(Paths.get(file.getPath())));
    }

    private static org.apache.http.HttpResponse post(String json) throws Exception {
        org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();

        // will need to allow IP to post to this "sudo vi /etc/nginx/conf.d/patientview-nginx.conf" then
        // restart with "sudo service nginx restart"
        //String postUrl="https://test.patientview.org/importer/import";
        //String postUrl="https://production.patientview.org/importer/import";
        String postUrl="http://localhost:8081/importer/import";

        HttpPost post = new HttpPost(postUrl);
        StringEntity postingString = new StringEntity(json);
        post.setEntity(postingString);
        post.setHeader("Content-type", "application/xml");
        HttpResponse httpResponse = httpClient.execute(post);
        return httpResponse;
    }
}
