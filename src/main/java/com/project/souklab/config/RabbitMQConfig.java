package com.project.souklab.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMQConfig {

    public static final String AUDIT_QUEUE = "audit.queue";
    public static final String EXCHANGE = "souklab.exchange";
    public static final String AUDIT_ROUTING_KEY = "audit.routing.key";

    @Bean
    public Queue auditQueue() {
        return new Queue(AUDIT_QUEUE, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, DirectExchange exchange) {
        return BindingBuilder.bind(auditQueue).to(exchange).with(AUDIT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
