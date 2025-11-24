package cl.huertohogar.huertohogar_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Huerto Hogar API")
                        .version("1.0.0")
                        .description("API REST para la gestión de Huerto Hogar. " +
                                "Este backend proporciona endpoints completos para la gestión de productos, usuarios y pedidos, " +
                                "compatible con clientes móviles (Kotlin) y web (React).\n\n" +
                                "**Características:**\n" +
                                "- Autenticación JWT para seguridad\n" +
                                "- CRUD completo para Productos, Usuarios y Pedidos\n" +
                                "- Soporte CORS para clientes web y móviles\n" +
                                "- Validaciones de datos en todas las operaciones\n" +
                                "- Arquitectura escalable y fácil de mantener")
                        .contact(new Contact()
                                .name("Huerto Hogar Team")
                                .email("info@huertohogar.cl"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desarrollo"),
                        new Server()
                                .url("https://api.huertohogar.cl")
                                .description("Servidor de Producción")
                ))
                .components(new Components().addSecuritySchemes(schemeName,
                        new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingrese el token JWT obtenido del endpoint /auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName));
    }
}
