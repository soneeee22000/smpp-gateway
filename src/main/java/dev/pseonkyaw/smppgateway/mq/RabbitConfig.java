package dev.pseonkyaw.smppgateway.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "sms.inbound";
    public static final String QUEUE = "sms.inbound.queue";
    public static final String ROUTING_KEY = "sms.inbound.*";

    @Bean
    public TopicExchange inboundExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue inboundQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding inboundBinding(Queue inboundQueue, TopicExchange inboundExchange) {
        return BindingBuilder.bind(inboundQueue).to(inboundExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
