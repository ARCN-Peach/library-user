package com.library.user.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.web.method.HandlerMethod;
import com.library.user.interfaces.http.OpenApiConfiguration;
import org.springdoc.core.customizers.OperationCustomizer;

class ConfigurationBeansTest {

    @Test
    void applicationConfigurationExposesUtcClock() {
        var configuration = new ApplicationConfiguration();

        assertThat(configuration.clock().getZone().getId()).isEqualTo("Z");
    }

    @Test
    void rabbitMqConfigurationCreatesExpectedBeans() {
        var configuration = new RabbitMqConfiguration();

        DirectExchange userExchange = configuration.userExchange();
        DirectExchange fineExchange = configuration.fineExchange();
        Queue fineGeneratedQueue = configuration.fineGeneratedQueue();
        Queue finePaidQueue = configuration.finePaidQueue();

        assertThat(userExchange.getName()).isEqualTo(RabbitMqConfiguration.USER_EXCHANGE);
        assertThat(fineExchange.getName()).isEqualTo(RabbitMqConfiguration.FINE_EXCHANGE);
        assertThat(fineGeneratedQueue.getName()).isEqualTo("user.fine-generated");
        assertThat(finePaidQueue.getName()).isEqualTo("user.fine-paid");
        assertThat(configuration.fineGeneratedBinding(fineGeneratedQueue, fineExchange).getRoutingKey())
                .isEqualTo(RabbitMqConfiguration.FINE_GENERATED_ROUTING_KEY);
        assertThat(configuration.finePaidBinding(finePaidQueue, fineExchange).getRoutingKey())
                .isEqualTo(RabbitMqConfiguration.FINE_PAID_ROUTING_KEY);
    }

    @Test
    void openApiConfigurationBuildsSpecAndHeaderCustomizer() {
        var configuration = new OpenApiConfiguration();

        OpenAPI openApi = invoke(configuration, "openAPI", OpenAPI.class);
        var customizer = invoke(configuration, "correlationIdHeaderCustomizer", OperationCustomizer.class);
        var operation = customizer.customize(new io.swagger.v3.oas.models.Operation(), (HandlerMethod) null);

        assertThat(openApi.getInfo().getTitle()).isEqualTo("library-user API");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("v1");
        assertThat(operation.getParameters()).singleElement().satisfies(parameter -> {
            assertThat(parameter.getName()).isEqualTo("X-Correlation-Id");
            assertThat(parameter.getRequired()).isTrue();
        });
    }

    @Test
    void configurationRecordsExposeAssignedValues() {
        var outbox = new OutboxProperties(25, "library.user.exchange");
        var jwt = new JwtProperties("secret", 900);
        var bootstrap = new BootstrapLibrarianProperties(true, "Admin", "admin@test.com", "Password123");

        assertThat(outbox.batchSize()).isEqualTo(25);
        assertThat(outbox.exchange()).isEqualTo("library.user.exchange");
        assertThat(jwt.secret()).isEqualTo("secret");
        assertThat(jwt.accessTokenTtlSeconds()).isEqualTo(900);
        assertThat(bootstrap.enabled()).isTrue();
        assertThat(bootstrap.email()).isEqualTo("admin@test.com");
    }

    private <T> T invoke(Object target, String methodName, Class<T> returnType) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            return returnType.cast(method.invoke(target));
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
}
