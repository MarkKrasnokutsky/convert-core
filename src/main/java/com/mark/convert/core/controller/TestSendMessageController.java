package com.mark.convert.core.controller;

import com.mark.convert.core.messaging.consumer.ConvertRequestConsumer;
import com.mark.convert.core.messaging.domain.ConvertRequestMessage;
import com.mark.convert.core.messaging.producer.ConvertRequestProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka")
@RequiredArgsConstructor
public class TestSendMessageController {

    private final ConvertRequestProducer convertRequestProducer;

    @PostMapping("/send")
    public String sendMessage(@RequestBody ConvertRequestMessage message) {
        convertRequestProducer.send(message);
        return "Message sent: " + message;
    }

}
