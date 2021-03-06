package com.rozhak.kafka.producer.controller;

import com.rozhak.kafka.model.Message;
import com.rozhak.kafka.model.NotExactlyAMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("message")
@Slf4j
public class MessagePublisher {

    @Value("${application.kafka.topicName}")
    private String stringTopicName;

    private static final String SUCCESS_MESSAGE = "Message was successfully published to Kafka: {}";

    private final KafkaTemplate<String, String> regularKafkaTemplate;
    private final KafkaTemplate<String, String> messageKafkaTemplate;
    private final KafkaTemplate<String, String> notExactlyAMessageKafkaTemplate;

    @Autowired
    public MessagePublisher(KafkaTemplate<String, String> regularKafkaTemplate,
                            KafkaTemplate<String, String> messageKafkaTemplate,
                            KafkaTemplate<String, String> notExactlyAMessageKafkaTemplate) {
        this.regularKafkaTemplate = regularKafkaTemplate;
        this.messageKafkaTemplate = messageKafkaTemplate;
        this.notExactlyAMessageKafkaTemplate = notExactlyAMessageKafkaTemplate;
    }

    @GetMapping("/publish/{message}")
    public String publishRegularMessageToKafka(@PathVariable("message") String message) {
        final String messageKey = UUID.randomUUID().toString();
        regularKafkaTemplate.send(stringTopicName, messageKey, message);
        log.info(SUCCESS_MESSAGE, message);
        return "Message was successfully published to Kafka:" + message;
    }

    @GetMapping("/publishAsJson/{message}")
    public String publishJsonMessageToKafka(@PathVariable("message") String message) {
        final Message messageObject = new Message(message, "defaultTag");
        final GenericMessage<Message> genericMessage = new GenericMessage<>(messageObject);
        messageKafkaTemplate.send(genericMessage);
        log.info(SUCCESS_MESSAGE, message);
        return "generated JSON Message was successfully published to Kafka:" + message;
    }

    @PostMapping(value = "/publishAsJson")
    public String publishJsonMessageToKafka(@RequestBody Message message) {
        final GenericMessage<Message> genericMessage = new GenericMessage<>(message);
        messageKafkaTemplate.send(genericMessage);
        log.info(SUCCESS_MESSAGE, message);
        return "JSON Message was successfully published to Kafka:" + message;
    }

    @GetMapping(value = "/publishAsJsonWithNotSpecifiedClass/{message}")
    public String publishAsJsonWithNotSpecifiedClassToKafka(@PathVariable("message") String message) {
        final NotExactlyAMessage messageObject = new NotExactlyAMessage(message, "defaultTagFrom");
        final GenericMessage<NotExactlyAMessage> genericMessage = new GenericMessage<>(messageObject);
        notExactlyAMessageKafkaTemplate.send(genericMessage);
        log.info(SUCCESS_MESSAGE, message);
        return "JSON GenericMessage was successfully published to Kafka:" + message;
    }

}
