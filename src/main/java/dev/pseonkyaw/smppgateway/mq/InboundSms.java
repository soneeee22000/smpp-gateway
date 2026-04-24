package dev.pseonkyaw.smppgateway.mq;

import java.io.Serializable;
import java.time.Instant;

public record InboundSms(
        String messageId,
        String sourceAddress,
        String destinationAddress,
        String text,
        String systemId,
        Instant receivedAt
) implements Serializable {
}
