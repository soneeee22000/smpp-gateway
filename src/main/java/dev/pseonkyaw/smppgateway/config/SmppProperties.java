package dev.pseonkyaw.smppgateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smpp")
public record SmppProperties(
        int port,
        int processorDegree,
        long bindTimeoutMs,
        String systemId,
        String password
) {
    public SmppProperties {
        if (port == 0) port = 2775;
        if (processorDegree == 0) processorDegree = 4;
        if (bindTimeoutMs == 0) bindTimeoutMs = 15_000L;
        if (systemId == null || systemId.isBlank()) systemId = "gateway";
        if (password == null) password = "gateway";
    }
}
