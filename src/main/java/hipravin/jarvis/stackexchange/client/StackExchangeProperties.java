package hipravin.jarvis.stackexchange.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@ConfigurationProperties(prefix = "stackexchange")
@Validated
public record StackExchangeProperties(
        @NotBlank String apiBaseUrl,
        @NotBlank @Name("excerpts.url") String excerptUrl,
        @Positive @Name("connection.timeout") long connectionTimeoutMillis,
        @Name("excerpts.params") Map<String, String> searchExcerptsParams
) {
    @ConstructorBinding
    public StackExchangeProperties {
    }
}
