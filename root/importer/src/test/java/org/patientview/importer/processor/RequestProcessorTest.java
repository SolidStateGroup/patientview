package org.patientview.importer.processor;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import generated.Patientview;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.patientview.importer.BaseTest;
import org.patientview.importer.service.PatientService;

public class RequestProcessorTest extends BaseTest {

    @Mock
    Channel channel;

    @Mock
    Envelope envelope;

    @Mock
    QueueingConsumer.Delivery delivery;

    @Mock
    QueueingConsumer queueingConsumer;

    @Mock
    PatientService patientService;

    @InjectMocks
    RequestProcessor requestProcessor;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test: This is test the importer picking of the patient records from the queue
     *
     * @throws Exception
     */
    @Test
    public void testProcess() throws Exception {
        Mockito.when(queueingConsumer.nextDelivery()).thenReturn(delivery);
        Mockito.when(delivery.getBody()).thenReturn(getTestFile().getBytes());
        Mockito.when(delivery.getEnvelope()).thenReturn(envelope);
        Mockito.when(queueingConsumer.getChannel()).thenReturn(channel);
       // Mockito.when(channel.close()).thenThrow(new InterruptedException());
        Thread thread = new Thread(requestProcessor);
        thread.start();
        Thread.currentThread().sleep(2000L);
        Mockito.verify(patientService, Mockito.atLeastOnce()).add(Mockito.any(Patientview.class));
        requestProcessor.shutdown();
    }

}