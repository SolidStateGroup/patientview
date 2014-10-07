package org.patientview.api.service;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.FhirDiagnosticReport;
import org.patientview.api.service.impl.DiagnosticServiceImpl;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.DiagnosticReportTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.patientview.persistence.repository.ObservationHeadingGroupRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.ResultClusterRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.test.util.TestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/10/2014
 */
public class DiagnosticServiceTest {

    User creator;

    @Mock
    ObservationHeadingRepository observationHeadingRepository;

    @Mock
    ObservationHeadingGroupRepository observationHeadingGroupRepository;

    @Mock
    GroupService groupService;

    @Mock
    UserRepository userRepository;

    @Mock
    ResultClusterRepository resultClusterRepository;


    @Mock
    PatientService patientService;

    @Mock
    FhirResource fhirResource;

    @InjectMocks
    DiagnosticService diagnosticService = new DiagnosticServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @Test
    public void testGetByUserId() {

        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier);
        fhirLink.setResourceType(ResourceType.Patient.name());
        fhirLink.setGroup(group);

        DiagnosticReport diagnosticReport = new DiagnosticReport();

        DateTime diagnosticDate = new DateTime();
        DateAndTime date = new DateAndTime(new Date());
        diagnosticDate.setValue(date);
        diagnosticReport.setDiagnostic(diagnosticDate);

        CodeableConcept name = new CodeableConcept();
        name.setTextSimple("EXAMPLE_DIAGNOSTIC_NAME");
        diagnosticReport.setName(name);

        CodeableConcept type = new CodeableConcept();
        type.setTextSimple(DiagnosticReportTypes.IMAGING.toString());
        diagnosticReport.setServiceCategory(type);

        diagnosticReport.setStatusSimple(DiagnosticReport.DiagnosticReportStatus.registered);

        ResourceReference resultReference = diagnosticReport.addResult();
        resultReference.setDisplaySimple("12345678-230a-4ce0-879b-443154a4d9e6");

        List<DiagnosticReport> diagnosticReports = new ArrayList<>();
        diagnosticReports.add(diagnosticReport);

        // create JSON object for observation (used for result)
        JSONObject resultJson = new JSONObject();
        String versionId = "31d2f326-230a-4ce0-879b-443154a4d9e6";
        String resourceId = "d52847eb-c2c7-4015-ba6c-952962536287";

        JSONObject link = new JSONObject();
        link.put("href", "http://www.patientview.org/patient/" + versionId);

        JSONArray links = new JSONArray();
        links.put(link);

        JSONObject valueQuantity = new JSONObject();
        valueQuantity.put("value", "99.9");

        JSONObject resultName = new JSONObject();
        JSONArray resultNameCoding = new JSONArray();
        JSONObject resultNameCodingDisplay = new JSONObject();
        resultName.put("text", NonTestObservationTypes.DIAGNOSTIC_RESULT.toString());
        resultNameCodingDisplay.put("display", NonTestObservationTypes.DIAGNOSTIC_RESULT.getName());
        resultNameCoding.put(0, resultNameCodingDisplay);
        resultName.put("coding", resultNameCoding);

        JSONObject resource = new JSONObject();
        resource.put("link", links);
        resource.put("id", resourceId);
        resource.put("resourceType", "Observation");
        resource.put("valueQuantity", valueQuantity);
        resource.put("name", resultName);

        JSONObject content = new JSONObject();
        content.put("content", resource);

        JSONArray resultArray = new JSONArray();
        resultArray.put(content);

        resultJson.put("entry", resultArray);

        try {
            when(userRepository.findOne(Matchers.eq(user.getId()))).thenReturn(user);
            when(fhirResource.findResourceByQuery(any(String.class), eq(DiagnosticReport.class)))
                    .thenReturn(diagnosticReports);
            when(fhirResource.getResource(any(UUID.class), eq(ResourceType.Observation))).thenReturn(resultJson);

            List<FhirDiagnosticReport> fhirDiagnosticReports = diagnosticService.getByUserId(user.getId());
            Assert.assertEquals("Should return 1 FhirDiagnostic", 1, fhirDiagnosticReports.size());
            Assert.assertEquals("Should have correct value", "99.9", fhirDiagnosticReports.get(0).getResult().getValue());

        } catch (ResourceNotFoundException rnf) {
            Assert.fail("ResourceNotFoundException: " + rnf.getMessage());
        } catch (FhirResourceException fre) {
            Assert.fail("FhirResourceException: " + fre.getMessage());
        }
    }
}
