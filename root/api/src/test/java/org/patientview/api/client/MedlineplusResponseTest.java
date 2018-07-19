package org.patientview.api.client;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;


/**
 * Created by pmaksymchuk on 10/08/2016.
 */
public class MedlineplusResponseTest {

    private static final String RESPONSE_JSON = "{\n" +
            "    \"feed\": {\n" +
            "        \"xsi\": \"http://www.w3.org/2001/XMLSchema-instance\", \n" +
            "        \"base\": \"https://medlineplus.gov/\", \n" +
            "        \"lang\": \"en\", \n" +
            "        \"title\": {\n" +
            "            \"_value\": \"MedlinePlus Connect\", \n" +
            "            \"type\": \"text\"\n" +
            "        }, \n" +
            "        \"subtitle\": {\n" +
            "            \"_value\": \"MedlinePlus Connect results for ICD-10-CM L70\", \n" +
            "            \"type\": \"text\"\n" +
            "        }, \n" +
            "        \"author\": {\n" +
            "            \"name\": {\n" +
            "                \"_value\": \"National Library of Medicine\"\n" +
            "            }, \n" +
            "            \"uri\": {\n" +
            "                \"_value\": \"https://www.nlm.nih.gov\"\n" +
            "            }\n" +
            "        }, \n" +
            "        \"updated\": {\n" +
            "            \"_value\": \"2016-08-10T03:08:54Z\"\n" +
            "        }, \n" +
            "        \"category\": [\n" +
            "            {\n" +
            "                \"scheme\": \"mainSearchCriteria.v.c\", \n" +
            "                \"term\": \"L70\"\n" +
            "            }, \n" +
            "            {\n" +
            "                \"scheme\": \"mainSearchCriteria.v.cs\", \n" +
            "                \"term\": \"ICD10CM\"\n" +
            "            }, \n" +
            "            {\n" +
            "                \"scheme\": \"mainSearchCriteria.v.dn\", \n" +
            "                \"term\": \"\"\n" +
            "            }, \n" +
            "            {\n" +
            "                \"scheme\": \"InformationRecipient\", \n" +
            "                \"term\": \"PAT\"\n" +
            "            }\n" +
            "        ], \n" +
            "        \"id\": {\n" +
            "            \"_value\": \"\"\n" +
            "        }, \n" +
            "        \"entry\": [\n" +
            "            {\n" +
            "                \"lang\": \"en\", \n" +
            "                \"title\": {\n" +
            "                    \"_value\": \"Acne\", \n" +
            "                    \"type\": \"text\"\n" +
            "                }, \n" +
            "                \"link\": [\n" +
            "                    {\n" +
            "                        \"title\": \"Acne\", \n" +
            "                        \"rel\": \"alternate\", \n" +
            "                        \"type\": \"html\", \n" +
            "                        \"href\": \"https://medlineplus.gov/acne.html\"\n" +
            "                    }\n" +
            "                ], \n" +
            "                \"id\": {\n" +
            "                    \"_value\": \"tag: https:, 2016-10-08:https://medlineplus.gov/acne.html\"\n" +
            "                }, \n" +
            "                \"updated\": {\n" +
            "                    \"_value\": \"2016-08-10T03:08:54Z\"\n" +
            "                }, \n" +
            "                \"summary\": {\n" +
            "                    \"_value\": \"<p class=\\\"NLMalsoCalled\\\">Also called:   Pimples, Zits</p><p>Acne is a common skin disease that causes pimples. Pimples form when hair follicles under your skin clog up. Most pimples form on the face, neck, back, chest, and shoulders. Anyone can get acne, but it is common in teenagers and young adults. It is not serious, but it can cause <a href=\\\"https://medlineplus.gov/scars.html\\\">scars</a>.</p><p>No one knows exactly what causes acne. Hormone changes, such as those during the teenage years and pregnancy, probably play a role. There are many myths about what causes acne. Chocolate and greasy foods are often blamed, but there is little evidence that foods have much effect on acne in most people. Another common myth is that dirty skin causes acne; however, blackheads and pimples are not caused by dirt. Stress doesn't cause acne, but stress can make it worse.</p><p>If you have acne</p><ul><li>Clean your skin gently </li><li>Try not to touch your skin </li><li>Avoid the sun </li></ul><p>Treatments for acne include medicines and creams.</p><p class=\\\"NLMattribution\\\">   NIH: National Institute of Arthritis and Musculoskeletal and Skin Diseases</p> <p class=\\\"NLMrelatedLinks\\\"><ul><li><a href=\\\"https://medlineplus.gov/ency/article/000873.htm\\\">Acne</a></li><li><a href=\\\"https://medlineplus.gov/ency/patientinstructions/000750.htm\\\">Acne -- self-care</a></li></ul></p>\", \n" +
            "                    \"type\": \"html\"\n" +
            "                }\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    @Test
    public void testParse_RESPONCE_SUCCESS() throws IOException {

        MedlineplusResponseJson json = new MedlineplusResponseJson();
        json.parse(RESPONSE_JSON);
        Assert.assertNotNull("Should get feed in response", json.getFeed());
        Assert.assertNotNull("Should get language in feed", json.getFeed().getLang());
        Assert.assertNotNull("Should get title in feed", json.getFeed().getTitle());
        Assert.assertNotNull("Should get title in feed", json.getFeed().getTitle().getValue());
        Assert.assertNotNull("Should get subtitle in feed", json.getFeed().getSubtitle().getValue());
        Assert.assertNotNull("Should get entry in feed", json.getFeed().getEntry());
        Assert.assertTrue("Should get entry in feed", json.getFeed().getEntry().length > 0);

        // check entry in Feed json
        EntryJson entryJson = json.getFeed().getEntry()[0];
        Assert.assertNotNull("Should get title in entry", entryJson.getTitle());
        Assert.assertNotNull("Should get title in entry", entryJson.getTitle().getValue());

        // check for Links, the actual data we need
        LinkJson linkJson = entryJson.getLink()[0];
        Assert.assertNotNull("Should get link in entry", linkJson);
        Assert.assertNotNull("Should get rel in link", linkJson.getRel());
        Assert.assertNotNull("Should get href in link", linkJson.getHref());
    }
}
