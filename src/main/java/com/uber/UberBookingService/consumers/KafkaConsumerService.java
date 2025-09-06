package com.uber.UberBookingService.consumers;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(groupId = "uber-group", topics="driverResponse-topic")
    public void listen(String message) {
        System.out.println("Kafka message from driverResponse topic: " + message);
    }

}
