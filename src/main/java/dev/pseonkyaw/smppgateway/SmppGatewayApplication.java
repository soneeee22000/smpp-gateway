package dev.pseonkyaw.smppgateway;

import dev.pseonkyaw.smppgateway.config.SmppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SmppProperties.class)
public class SmppGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmppGatewayApplication.class, args);
	}

}
