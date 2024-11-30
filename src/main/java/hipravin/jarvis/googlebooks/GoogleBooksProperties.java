package hipravin.jarvis.googlebooks;


import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "googlebooks")
@Validated
public record GoogleBooksProperties(
        @NotBlank String booksSearchUrl,
        String apiKey
) {
    @ConstructorBinding
    public GoogleBooksProperties {
    }
}
