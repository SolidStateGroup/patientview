package org.patientview.api.client.nhschoices;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;


/**
 * Unit test for NHSChoices response json
 */
public class NhsChoicesResponseTest {

    private static final String RESPONSE_JSON = "{\n" +
            "    \"interactionStatistic\": [\n" +
            "        {\n" +
            "            \"interactionService\": {\n" +
            "                \"url\": \"some urls\",\n" +
            "                \"@type\": \"Website\",\n" +
            "                \"name\": \"Webtrends tracking pixel\"\n" +
            "            },\n" +
            "            \"@type\": \"InteractionCounter\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"license\": \"https://www.nhs.uk/aboutNHSChoices/professionals/syndication/Documents/nhs-choices-standard-licence-terms.pdf\",\n" +
            "    \"author\": {\n" +
            "        \"url\": \"https://www.nhs.uk\",\n" +
            "        \"logo\": \"https://www.nhs.uk/nhscwebservices/documents/logo1.width-610.jpg\",\n" +
            "        \"email\": \"nhschoicesservicedesk@nhs.net\",\n" +
            "        \"@type\": \"Organization\",\n" +
            "        \"name\": \"NHS Choices\"\n" +
            "    },\n" +
            "    \"url\": \"https://www.nhs.uk/conditions/\",\n" +
            "    \"copyrightholder\": {\n" +
            "        \"name\": \"Crown Copyright\",\n" +
            "        \"@type\": \"Organization\"\n" +
            "    },\n" +
            "    \"significantLink\": [\n" +
            "        {\n" +
            "            \"name\": \"A limp in a child\",\n" +
            "            \"url\": \"https://api.nhs.uk/conditions/limp-in-children/\",\n" +
            "            \"linkRelationship\": \"Result\",\n" +
            "            \"mainEntityOfPage\": {\n" +
            "                \"lastReviewed\": [\n" +
            "                    \"2016-03-29T00:00:00Z\",\n" +
            "                    \"2018-10-31T00:00:00Z\"\n" +
            "                ],\n" +
            "                \"@type\": \"MedicalWebPage\",\n" +
            "                \"genre\": \"condition\",\n" +
            "                \"keywords\": [\n" +
            "                    \"Thigh\",\n" +
            "                    \"Joints\",\n" +
            "                    \"Nerves\",\n" +
            "                    \"Bone infections\",\n" +
            "                    \"Joint pain\",\n" +
            "                    \"Arthritis\",\n" +
            "                    \"Irritable hip\",\n" +
            "                    \"Older people\",\n" +
            "                    \"Hip\",\n" +
            "                    \"Bones\",\n" +
            "                    \"Scoliosis\",\n" +
            "                    \"Children\",\n" +
            "                    \"Swollen joints\"\n" +
            "                ],\n" +
            "                \"datePublished\": \"2017-10-19T12:52:42.334Z\",\n" +
            "                \"dateModified\": \"2017-10-19T12:52:42.361Z\"\n" +
            "            },\n" +
            "            \"@type\": \"LinkRole\",\n" +
            "            \"description\": \"If a child is limping, the limp is usually due to a minor injury such as a sprain or splinter. But if there's no obvious cause, see your GP.\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"Abdominal aortic aneurysm\",\n" +
            "            \"url\": \"https://api.nhs.uk/conditions/abdominal-aortic-aneurysm/\",\n" +
            "            \"linkRelationship\": \"Result\",\n" +
            "            \"mainEntityOfPage\": {\n" +
            "                \"lastReviewed\": [\n" +
            "                    \"2017-07-04T00:00:00Z\",\n" +
            "                    \"2020-07-04T00:00:00Z\"\n" +
            "                ],\n" +
            "                \"@type\": \"MedicalWebPage\",\n" +
            "                \"genre\": \"condition\",\n" +
            "                \"keywords\": [\n" +
            "                    \"Aortic aneurysm\",\n" +
            "                    \"Repair of abdominal aortic aneurysm\",\n" +
            "                    \"Hypertension\"\n" +
            "                ],\n" +
            "                \"datePublished\": \"2017-10-20T11:42:38.012Z\",\n" +
            "                \"dateModified\": \"2017-10-25T10:38:59.752Z\"\n" +
            "            },\n" +
            "            \"@type\": \"LinkRole\",\n" +
            "            \"description\": \"Find out what an abdominal aortic aneurysm (AAA) is, what symptoms it can cause, who's at risk of getting one, and how it can be treated.\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"Abdominal aortic aneurysm screening\",\n" +
            "            \"url\": \"https://api.nhs.uk/conditions/abdominal-aortic-aneurysm-screening/\",\n" +
            "            \"linkRelationship\": \"Result\",\n" +
            "            \"mainEntityOfPage\": {\n" +
            "                \"genre\": \"condition\",\n" +
            "                \"dateModified\": \"2017-10-20T10:38:09.878Z\",\n" +
            "                \"lastReviewed\": [\n" +
            "                    \"2017-07-24T00:00:00Z\",\n" +
            "                    \"2020-07-24T00:00:00Z\"\n" +
            "                ],\n" +
            "                \"@type\": \"MedicalWebPage\",\n" +
            "                \"datePublished\": \"2017-10-20T10:36:32.510Z\"\n" +
            "            },\n" +
            "            \"@type\": \"LinkRole\",\n" +
            "            \"description\": \"Find out who is offered abdominal aortic aneurysm (AAA) screening, why it's done and what it involves.\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"@context\": \"http://schema.org\",\n" +
            "    \"@type\": \"SearchResultsPage\"\n" +
            "}";

    @Test
    public void testParse_RESPONCE_SUCCESS() throws IOException {

        NhsChoicesResponseJson json = new NhsChoicesResponseJson();
        json.parse(RESPONSE_JSON);
        Assert.assertNotNull("Should get condition links in response", json.getConditionLinks());
        Assert.assertTrue("Should have condition links in response", json.getConditionLinks().size() > 0);

        for (ConditionLink link : json.getConditionLinks()) {
            Assert.assertNotNull("Should get condition name", link.getName());
            Assert.assertNotNull("Should get condition url", link.getUrl());
            Assert.assertNotNull("Should get condition description", link.getDescription());
        }
    }
}
