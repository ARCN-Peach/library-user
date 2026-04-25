package com.library.user.interfaces.http;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("library-user API")
                        .version("v1")
                        .description("User microservice for ARCN library"));
    }

    @Bean
    OperationCustomizer correlationIdHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            var alreadyDefined = operation.getParameters() != null
                    && operation.getParameters().stream()
                    .anyMatch(parameter -> "X-Correlation-Id".equalsIgnoreCase(parameter.getName()));

            if (!alreadyDefined) {
                operation.addParametersItem(new Parameter()
                        .in("header")
                        .required(false)
                        .name("X-Correlation-Id")
                        .description("Optional correlation id; generated automatically if omitted."));
            }
            return operation;
        };
    }
}
