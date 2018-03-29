package org.patientview.api.client.nhschoices;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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


    private static final String GP_ORGANISATION_JSON = "{\n" +
            "    \"version\": \"1.0\",\n" +
            "    \"encoding\": \"utf-8\",\n" +
            "    \"Organisation\": {\n" +
            "        \"@xmlns$i\": \"http://www.w3.org/2001/XMLSchema-instance\",\n" +
            "        \"@xmlns\": \"http://schemas.datacontract.org/2004/07/NHSChoices.Syndication.Resources.Orgs\",\n" +
            "        \"Link\": {\n" +
            "            \"@xmlns\": \"http://schemas.datacontract.org/2004/07/NHSChoices.Syndication.Resources\",\n" +
            "            \"Text\": null,\n" +
            "            \"Uri\": \"https://api.nhs.uk/data/gppractices/odscode/E85074\"\n" +
            "        },\n" +
            "        \"Address\": {\n" +
            "            \"Line1\": \"15 Brook Green\",\n" +
            "            \"Line2\": {\n" +
            "                \"@i$nil\": \"true\"\n" +
            "            },\n" +
            "            \"Line3\": {\n" +
            "                \"@i$nil\": \"true\"\n" +
            "            },\n" +
            "            \"Line4\": \"London\",\n" +
            "            \"Line5\": \"Greater London\",\n" +
            "            \"Postcode\": \"W6 7BL\"\n" +
            "        },\n" +
            "        \"CommentUrl\": \"http://www.nhs.uk/Services/GP/ReviewsAndRatings/DefaultView.aspx?id=42973\",\n" +
            "        \"CqcNumber\": {\n" +
            "            \"@i$nil\": \"true\"\n" +
            "        },\n" +
            "        \"Easting\": \"0\",\n" +
            "        \"EmailAddress\": {\n" +
            "            \"@i$nil\": \"true\"\n" +
            "        },\n" +
            "        \"Fax\": {\n" +
            "            \"@i$nil\": \"true\"\n" +
            "        },\n" +
            "        \"FiveStarRecommendationRating\": {\n" +
            "            \"Value\": \"3.5384615384615383\",\n" +
            "            \"MaxValue\": \"5\",\n" +
            "            \"MinValue\": \"1\",\n" +
            "            \"NumberOfRatings\": \"13\"\n" +
            "        },\n" +
            "        \"Latitude\": \"51.495292663574219\",\n" +
            "        \"Longitude\": \"-0.21813033521175385\",\n" +
            "        \"ModifiedDate\": \"2013-01-08T04:30:24.58\",\n" +
            "        \"Name\": \"Brook Green Surgery\",\n" +
            "        \"Northing\": \"0\",\n" +
            "        \"OdsCode\": \"E85074\",\n" +
            "        \"OrganisationId\": \"11926\",\n" +
            "        \"OrganisationSubTypeId\": {\n" +
            "            \"@i$nil\": \"true\"\n" +
            "        },\n" +
            "        \"OrganisationTypeId\": {\n" +
            "            \"@i$nil\": \"true\"\n" +
            "        },\n" +
            "        \"OrganisationTypeName\": \"gppractices\",\n" +
            "        \"OverallRating\": {\n" +
            "            \"@i$nil\": \"true\"\n" +
            "        },\n" +
            "        \"ParentOrganisationCode\": {\n" +
            "            \"@i$nil\": \"true\"\n" +
            "        },\n" +
            "        \"ParentOrganisationName\": {\n" +
            "            \"@i$nil\": \"true\"\n" +
            "        },\n" +
            "        \"PimsOrganisationTypeId\": \"0\",\n" +
            "        \"ProfileLinks\": {\n" +
            "            \"@xmlns$d2p1\": \"http://schemas.datacontract.org/2004/07/NHSChoices.Syndication.Resources\",\n" +
            "            \"d2p1$Link\": [\n" +
            "                {\n" +
            "                    \"d2p1$Class\": \"profile\",\n" +
            "                    \"d2p1$Text\": \"Overview\",\n" +
            "                    \"d2p1$Uri\": \"https://api.nhs.uk/data/gppractices/11926/overview\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"d2p1$Class\": \"profile\",\n" +
            "                    \"d2p1$Text\": \"Services\",\n" +
            "                    \"d2p1$Uri\": \"https://api.nhs.uk/data/gppractices/11926/services\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"d2p1$Class\": \"profile\",\n" +
            "                    \"d2p1$Text\": \"Staff\",\n" +
            "                    \"d2p1$Uri\": \"https://api.nhs.uk/data/gppractices/11926/staff\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"d2p1$Class\": \"profile\",\n" +
            "                    \"d2p1$Text\": \"Facilities\",\n" +
            "                    \"d2p1$Uri\": \"https://api.nhs.uk/data/gppractices/11926/facilities\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"d2p1$Class\": \"profile\",\n" +
            "                    \"d2p1$Text\": \"Core Indicators\",\n" +
            "                    \"d2p1$Uri\": \"https://api.nhs.uk/data/gppractices/11926/indicators/core\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"d2p1$Class\": \"profile\",\n" +
            "                    \"d2p1$Text\": \"Treatment Indicators\",\n" +
            "                    \"d2p1$Uri\": \"https://api.nhs.uk/data/gppractices/11926/indicators/treatment\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"d2p1$Class\": \"profile\",\n" +
            "                    \"d2p1$Text\": \"Faq\",\n" +
            "                    \"d2p1$Uri\": \"https://api.nhs.uk/data/gppractices/11926/faq\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"d2p1$Class\": \"profile\",\n" +
            "                    \"d2p1$Text\": \"Performance\",\n" +
            "                    \"d2p1$Uri\": \"https://api.nhs.uk/data/gppractices/11926/performance\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"d2p1$Class\": \"profile\",\n" +
            "                    \"d2p1$Text\": \"Contact\",\n" +
            "                    \"d2p1$Uri\": \"https://api.nhs.uk/data/gppractices/11926/contact\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"d2p1$Class\": \"comments\",\n" +
            "                    \"d2p1$Text\": \"Comments Before\",\n" +
            "                    \"d2p1$Uri\": \"https://api.nhs.uk/data/gppractices/11926/commentsbefore\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"d2p1$Class\": \"comments\",\n" +
            "                    \"d2p1$Text\": \"Comments Since\",\n" +
            "                    \"d2p1$Uri\": \"https://api.nhs.uk/data/gppractices/11926/commentssince\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"d2p1$Class\": \"comments\",\n" +
            "                    \"d2p1$Text\": \"All Comments\",\n" +
            "                    \"d2p1$Uri\": \"https://api.nhs.uk/data/gppractices/11926/comments\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"Ratings\": {\n" +
            "            \"rating\": [\n" +
            "                {\n" +
            "                    \"questionText\": \"Are you able to get through to the surgery by telephone?\",\n" +
            "                    \"answerText\": \"Usually\",\n" +
            "                    \"answerMetric\": {\n" +
            "                        \"@value\": \"3.92307692307692\",\n" +
            "                        \"@type\": \"Average\",\n" +
            "                        \"@minValue\": \"1\",\n" +
            "                        \"@maxValue\": \"5\",\n" +
            "                        \"@numberOfRatings\": \"13\"\n" +
            "                    }\n" +
            "                },\n" +
            "                {\n" +
            "                    \"questionText\": \"Are you able to get an appointment when you want one?\",\n" +
            "                    \"answerText\": \"Sometimes\",\n" +
            "                    \"answerMetric\": {\n" +
            "                        \"@value\": \"3\",\n" +
            "                        \"@type\": \"Average\",\n" +
            "                        \"@minValue\": \"1\",\n" +
            "                        \"@maxValue\": \"5\",\n" +
            "                        \"@numberOfRatings\": \"13\"\n" +
            "                    }\n" +
            "                },\n" +
            "                {\n" +
            "                    \"questionText\": \"Do the staff treat you with dignity and respect?\",\n" +
            "                    \"answerText\": \"Usually\",\n" +
            "                    \"answerMetric\": {\n" +
            "                        \"@value\": \"3.92307692307692\",\n" +
            "                        \"@type\": \"Average\",\n" +
            "                        \"@minValue\": \"1\",\n" +
            "                        \"@maxValue\": \"5\",\n" +
            "                        \"@numberOfRatings\": \"13\"\n" +
            "                    }\n" +
            "                },\n" +
            "                {\n" +
            "                    \"questionText\": \"Does the surgery involve you in decisions about your care and treatment?\",\n" +
            "                    \"answerText\": \"Usually\",\n" +
            "                    \"answerMetric\": {\n" +
            "                        \"@value\": \"4\",\n" +
            "                        \"@type\": \"Average\",\n" +
            "                        \"@minValue\": \"1\",\n" +
            "                        \"@maxValue\": \"5\",\n" +
            "                        \"@numberOfRatings\": \"13\"\n" +
            "                    }\n" +
            "                },\n" +
            "                {\n" +
            "                    \"questionText\": \"This GP practice provides accurate and up to date information on services and opening hours\",\n" +
            "                    \"answerText\": \"Usually\",\n" +
            "                    \"answerMetric\": {\n" +
            "                        \"@value\": \"3.76923076923077\",\n" +
            "                        \"@type\": \"Average\",\n" +
            "                        \"@minValue\": \"1\",\n" +
            "                        \"@maxValue\": \"5\",\n" +
            "                        \"@numberOfRatings\": \"13\"\n" +
            "                    }\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"Telephone\": \"02076037563\",\n" +
            "        \"UnModifiedOdsCode\": \"E85074\",\n" +
            "        \"WebAddress\": \"http://www.brookgreensurgery.nhs.uk\"\n" +
            "    }\n" +
            "}";

    @Test
    public void testParse_RESPONCE_SUCCESS() throws IOException {

        NhsChoicesResponseJson json = new NhsChoicesResponseJson();
        json.parse(RESPONSE_JSON);
        Assert.assertNotNull("Should get condition links in response", json.getConditionLinks());
        Assert.assertTrue("Should have condition links in response", json.getConditionLinks().size() > 0);

        for (ConditionLinkJson link : json.getConditionLinks()) {
            Assert.assertNotNull("Should get condition name", link.getName());
            Assert.assertNotNull("Should get condition url", link.getApiUrl());
            Assert.assertNotNull("Should get condition description", link.getDescription());
        }
    }

    @Test
    public void testParse_ORGANIZATION_SUCCESS() throws IOException {

        // get JSON for organisation from NHS choices
        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(GP_ORGANISATION_JSON);
        JsonObject rootobj = root.getAsJsonObject();
        JsonObject orgobj = rootobj.get("Organisation").getAsJsonObject();

        String organisationId = orgobj.get("OrganisationId").getAsString();
        String telephone = orgobj.get("Telephone").getAsString();

        Assert.assertNotNull("Should have OrganisationId", organisationId);
        Assert.assertNotNull("Should have Telephone", telephone);

    }
}
