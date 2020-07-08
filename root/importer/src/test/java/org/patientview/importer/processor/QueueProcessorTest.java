package org.patientview.importer.processor;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.Envelope;
import generated.Patientview;
import org.hl7.fhir.instance.model.ResourceReference;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.patientview.importer.BaseTest;
import org.patientview.service.PatientService;

import java.util.concurrent.ExecutorService;

public class QueueProcessorTest extends BaseTest {

    @Mock
    ExecutorService executorService;

    @Mock
    Channel channel;

    @Mock
    Envelope envelope;

    @Mock
    Delivery delivery;

    @Mock
    AMQP.BasicProperties basicProperties;

    @Mock
    PatientService patientService;

    @InjectMocks
    QueueProcessor queueProcessor;

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
    @Ignore("broken")
    public void testProcess() throws Exception {

        //Mockito.when(queueingConsumer.nextDelivery()).thenReturn(delivery);
        Mockito.when(delivery.getBody()).thenReturn(getTestFile().getBytes());
        Mockito.when(delivery.getEnvelope()).thenReturn(envelope);
        //Mockito.when(queueingConsumer.getChannel()).thenReturn(channel);
       // Mockito.when(channel.close()).thenThrow(new InterruptedException());
       // Thread thread = new Thread(queueProcessor);
        //thread.start();
        Thread.currentThread().sleep(2000L);
        Mockito.verify(patientService, Mockito.atLeastOnce()).add(Mockito.any(Patientview.class)
                , Mockito.any(ResourceReference.class));
        queueProcessor.shutdown();
    }

    /**
     * Test: Passing the message on to the executor
     * Fail: The executor pool never gets called
     *
     * @throws Exception
     */
    @Test
    public void testConsume() throws Exception {
        queueProcessor.handleDelivery(null, envelope, basicProperties, super.getTestFile().getBytes());
        Mockito.verify(executorService, Mockito.atLeastOnce()).submit(Mockito.any(Runnable.class));
    }

}