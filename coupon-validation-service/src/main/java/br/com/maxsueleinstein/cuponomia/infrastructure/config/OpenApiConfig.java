package br.com.maxsueleinstein.cuponomia.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cuponomiaOpenAPI() {
        return new OpenAPI()
                .tags(List.of(
                        new Tag().name("Checkout Validation").description("Apply coupons during checkout and inspect validation results."),
                        new Tag().name("System").description("Service navigation and health helpers.")))
                .info(new Info()
                        .title("Cuponomia Validation API")
                        .description("Checkout-facing coupon validation service backed by an event-fed local coupon projection. "
                                + "Documentation: https://github.com/maxeinstein-dev/Cuponomia#readme")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Maxsuel Einstein")
                                .url("https://github.com/maxeinstein-dev")))
                .externalDocs(new ExternalDocumentation()
                        .description("README and demo guide")
                        .url("https://github.com/maxeinstein-dev/Cuponomia/blob/master/docs/DEMO_GUIDE.md"));
    }
}
