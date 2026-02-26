package hipravin.jarvis.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Jarvis API")
                        .version("0.9")
                        .description("Jarvis HTTP Api documentation")
                        .contact(new Contact()
                                .name("Dev team")
                                .email("dev@hipravin.info")));
    }
}