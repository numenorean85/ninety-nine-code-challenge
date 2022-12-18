package com.ninetynine.codechallenge.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun apiGroup(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("Api")
            .pathsToMatch("/api/**")
            .packagesToScan("com.ninetynine.codechallenge")
            .build()
    }

    @Bean
    fun apiInfo(): OpenAPI {
        return OpenAPI()
            .info(Info()
                .title("Spring Boot2 REST API")
                .description("NinetyNine Code Challence")
                .version("1.0")
            )
    }
}