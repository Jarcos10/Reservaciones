package org.example.reservaciones.core.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BASIC_AUTH_SCHEME = "basicAuth";

    @Bean
    public OpenAPI reservacionesOpenAPI() {
        return new OpenAPI()
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(
                                BASIC_AUTH_SCHEME,
                                new SecurityScheme()
                                        .name(BASIC_AUTH_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                                        .description("Autenticación HTTP Basic. En Swagger se debe presionar Authorize e ingresar el correo y contraseña del usuario. El rol ADMIN permite administrar habitaciones y reservaciones; el rol USUARIO permite crear reservaciones.")
                        )
                )
                .info(new Info()
                        .title("API de Reservaciones")
                        .description("Documentación de los servicios REST del sistema de reservaciones. La API permite consultar habitaciones disponibles, registrar reservaciones, administrar el catálogo de cuartos y gestionar la seguridad mediante Spring Security con HTTP Basic y roles ADMIN / USUARIO.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Julio Cesar Arcos Salazar")
                                .email("julio.arcos.salazar@gmail.com"))
                        .license(new License()
                                .name("Uso académico")
                                .url("https://www.escom.ipn.mx/")));
    }
}
