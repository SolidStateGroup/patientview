package org.patientview.importer.service;

import com.rabbitmq.client.Channel;
import generated.Patientview;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.importer.rabbit.MessageProducer;
import org.patientview.importer.service.impl.QueueServiceImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.URL;

public class QueueServiceTest {

    @Mock
    Channel channel;

    @Mock
    MessageProducer messageProducer;

    @InjectMocks
    QueueService queueService = new QueueServiceImpl();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testImportRecord() throws Exception {
        queueService.importRecord(getPatientViewRecord());
    }

    private Patientview getPatientViewRecord() throws Exception {
        URL xmlPath =
                Thread.currentThread().getContextClassLoader().getResource("data/xml/SAC02_01436_14251692001.xml");
        File file = new File(xmlPath.toURI());

        JAXBContext jc = JAXBContext.newInstance(Patientview.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (Patientview) unmarshaller.unmarshal(file);

    }
}