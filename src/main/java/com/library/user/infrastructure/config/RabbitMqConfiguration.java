package com.library.user.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {

    public static final String USER_EXCHANGE = "library.user.exchange";
    public static final String FINE_EXCHANGE = "library.fine.exchange";
    public static final String USER_BLOCKED_ROUTING_KEY = "user.blocked.v1";
    public static final String USER_REGISTERED_ROUTING_KEY = "user.registered.v1";
    public static final String FINE_GENERATED_ROUTING_KEY = "fine.generated.v1";
    public static final String USER_DEBT_CLEARED_ROUTING_KEY = "user.debt-cleared.v1";

    @Bean
    DirectExchange userExchange() {
        return new DirectExchange(USER_EXCHANGE, true, false);
    }

    @Bean
    DirectExchange fineExchange() {
        return new DirectExchange(FINE_EXCHANGE, true, false);
    }

    @Bean
    Queue fineGeneratedQueue() {
        return new Queue("library-user.fine-generated.v1", true);
    }

    @Bean
    Queue userDebtClearedQueue() {
        return new Queue("library-user.user-debt-cleared.v1", true);
    }

    @Bean
    Binding fineGeneratedBinding(Queue fineGeneratedQueue, DirectExchange fineExchange) {
        return BindingBuilder.bind(fineGeneratedQueue).to(fineExchange).with(FINE_GENERATED_ROUTING_KEY);
    }

    @Bean
    Binding userDebtClearedBinding(Queue userDebtClearedQueue, DirectExchange fineExchange) {
        return BindingBuilder.bind(userDebtClearedQueue).to(fineExchange).with(USER_DEBT_CLEARED_ROUTING_KEY);
    }
}
