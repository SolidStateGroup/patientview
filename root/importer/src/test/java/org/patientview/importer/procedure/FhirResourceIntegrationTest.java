package org.patientview.importer.procedure;

import generated.Patientview;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceReference;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.patientview.importer.BaseTest;
import org.patientview.importer.builder.ObservationsBuilder;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.Utility.FhirTestUtil;
import org.patientview.importer.Utility.Util;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

public class FhirResourceIntegrationTest extends BaseTest {

    DataSource realDataSource;

    @Mock
    DataSource dataSource;

    @InjectMocks
    FhirResource fhirResource;

    public void setUp() throws Exception {
        super.setUp();
        realDataSource = new BasicDataSource();
        ((BasicDataSource) realDataSource).setDriverClassName("org.postgresql.Driver");
        ((BasicDataSource) realDataSource).setUrl("jdbc:postgresql://localhost:5432/fhir");
        ((BasicDataSource) realDataSource).setUsername("fhir");
        ((BasicDataSource) realDataSource).setPassword("fhir");

    }

    @Test
    @Ignore("Integration Test")
    public void testCreateResource() throws Exception {
        Mockito.when(dataSource.getConnection()).thenReturn(realDataSource.getConnection());
        System.out.println(fhirResource.create(FhirTestUtil.createTestPatient("1231321312")));
    }

    @Test
    @Ignore("Integration Test")
    public void testUpdateResource() throws Exception {
        Mockito.when(dataSource.getConnection()).thenReturn(realDataSource.getConnection());
        fhirResource.create(FhirTestUtil.createTestPatient("1231321312"));
    }

    @Test
    @Ignore("Integration Test")
    public void testInsertObservationResource() throws Exception {
        Mockito.when(dataSource.getConnection()).thenReturn(realDataSource.getConnection());

        Patientview patientview = Util.unmarshallPatientRecord(getTestFile());
        ResourceReference resourceReference = new ResourceReference();
        resourceReference.setReferenceSimple("uuid");
        resourceReference.setDisplaySimple(UUID.randomUUID().toString());
        ObservationsBuilder observationsBuilder = new ObservationsBuilder(patientview, resourceReference);
        List<Observation> observations = observationsBuilder.build();

        int i = 0;
        for (Observation observation : observations) {
            fhirResource.create(observation);
            System.out.println(i++);

        }
    }

    @org.junit.After
    public void tearDown() {
        realDataSource = null;
    }

}