package org.patientview.importer.procedure;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.patientview.importer.BaseTest;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.util.FhirTestUtil;
import org.patientview.importer.util.Util;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.UUID;

public class FhirResourceIntegrationTest extends BaseTest {

    DriverManagerDataSource realDataSource;

    @Mock
    DataSource dataSource;

    @InjectMocks
    FhirResource fhirResource;

    public void setUp() throws Exception {
        super.setUp();
        realDataSource = new DriverManagerDataSource();
        realDataSource.setDriverClassName("org.postgresql.Driver");
        realDataSource.setUrl("jdbc:postgresql://localhost:5432/fhir");
        realDataSource.setUsername("fhir");
        realDataSource.setPassword("fhir");

    }

    @Test
    //@Ignore("Integration Test")
    public void testCreateResource() throws Exception {
        Mockito.when(dataSource.getConnection()).thenReturn(realDataSource.getConnection());
        System.out.println(fhirResource.create(FhirTestUtil.createTestPatient("1231321312")));
    }

    @Test
   // @Ignore("Integration Test")
    public void testUpdateResource() throws Exception {
        Mockito.when(dataSource.getConnection()).thenReturn(realDataSource.getConnection());
        JSONObject jsonObject = fhirResource.create(FhirTestUtil.createTestPatient("1231321312"));
        UUID versionId =  fhirResource.update(Util.getResource(jsonObject), Util.getResourceId(jsonObject), Util.getVersionId(jsonObject));

        System.out.println("New Version ID " + versionId);
    }

}