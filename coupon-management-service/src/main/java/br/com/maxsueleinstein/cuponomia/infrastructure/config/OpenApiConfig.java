package br.com.maxsueleinstein.cuponomia.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cuponomiaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cuponomia API")
                        .description("Sistema de Cupons com Regras Dinâmicas — API REST para criação, "
                                + "gerenciamento e aplicação de cupons de desconto no checkout.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Maxsuel Einstein")
                                .url("https://github.com/maxsueleinstein")));
    }
}
