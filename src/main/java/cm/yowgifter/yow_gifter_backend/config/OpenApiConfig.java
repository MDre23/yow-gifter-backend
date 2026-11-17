package cm.yowgifter.yow_gifter_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Yow Gifter API — Backend Spring Boot 3")
                        .version("1.0.0")
                        .description("API REST pour la plateforme citoyenne et solidaire de dons gratuits d'objets (0 FCFA) à Yaoundé.\n" +
                                     "Projet ENSPY / UY1 4GI — UE Administration et Supervision des Réseaux (Prof Thomas Djotio Ndié).")
                        .contact(new Contact()
                                .name("Équipe Yow Gifter 4GI")
                                .email("contact@yowgifter.cm")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Entrez le token JWT obtenu via /api/v1/auth/login")));
    }
}
