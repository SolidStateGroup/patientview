package org.patientview;


//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * Created by james@solidstate on 15/05/2014.
 */
@Configuration
public class BaseApplication {

    private final static String queueName = "ssg";

//    @Autowired
//    RabbitTemplate rabbitTemplate;



    //public static void main(String[] args) throws InterruptedException {
       // SpringApplication.run(BaseApplication.class, args);
   /// }


    public void run(String... args) throws Exception {
        while (true) {
            Thread.sleep(1);
            System.out.println("Sending message...");
            Log log = new Log();
            log.setClassName("BaseApplication");
            log.setUsername("LogUser");
            log.setDate(new Date());
            log.setMessage("This is the log message");

//            rabbitTemplate.convertAndSend(queueName, log);
        }
    }
}