package org.patientview.importer.service;

import com.rabbitmq.client.Channel;
import generated.Patientview;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.importer.service.impl.ImportServiceImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.URL;

public class ImportServiceImplTest {

    @Mock
    Channel channel;

    @InjectMocks
    ImportService importService = new ImportServiceImpl();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testImportRecord() throws Exception {
        importService.importRecord(getPatientViewRecord());
    }

    private Patientview getPatientViewRecord() throws Exception {
        URL xmlPath =
                Thread.currentThread().getContextClassLoader().getResource("data/xml/SAC02_01436_21626578408.xml");
        File file = new File(xmlPath.toURI());

        JAXBContext jc = JAXBContext.newInstance(Patientview.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (Patientview) unmarshaller.unmarshal(file);

    }
}