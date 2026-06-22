package org.example.reservaciones.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {

    @Bean
    public OpenAPI myOpenAPI () {

        return new OpenAPI()
                .info(new Info()
                        .title("API para la gestión de reservaciones")
                        .version("1.0.0")
                        .description("API REST para la Gestión de Cuartos y Reservaciones")
                        .contact(new Contact()
                                .name("Julio Cesar Arcos Salazar")
                                .email("julio.arcos.salazar@gmail.com")
                                .url("Pagina personal")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("Url de la licencia")
                        )
                );
    }
}
