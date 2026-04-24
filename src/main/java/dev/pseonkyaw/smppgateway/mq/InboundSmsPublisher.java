package dev.pseonkyaw.smppgateway.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InboundSmsPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(InboundSms sms) {
        String routingKey = "sms.inbound." + safeKey(sms.destinationAddress());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, sms);
        log.info("published inbound sms messageId={} dst={}", sms.messageId(), sms.destinationAddress());
    }

    private String safeKey(String dst) {
        if (dst == null) return "unknown";
        return dst.replaceAll("[^0-9A-Za-z]", "");
    }
}
