package com.contest.notification.listener;

import com.contest.notification.dto.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

public class GenericConsumer implements Consumer {
    @KafkaListener(topics="${generic.kafka.topic}",containerFactory = "headerConcurrentKafkaListenerContainerFactory")
    public void receiveMessage(Header header) {
        LOGGER.info("Received:"+ header);
    }

    @Override
    public String processMessage(Header header) {
        return null;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericConsumer.class);

}