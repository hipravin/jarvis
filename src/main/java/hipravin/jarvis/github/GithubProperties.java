package hipravin.jarvis.github;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;


@ConfigurationProperties(prefix = "github")
@Validated
public record GithubProperties(
        @NotBlank String codeSearchUrl,
        @NotBlank String codeSearchBrowserUrlTemplate,
        @NotEmpty Set<String> approvedAuthors,
        @NotEmpty String token,
        @Positive @Name("connection.timeout") long connectionTimeoutMillis,
        int singleRequestMaxOr,
        int codeSearchPerPage
) {
    @ConstructorBinding
    public GithubProperties {
    }
}
