package hipravin.jarvis.github;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;


@ConfigurationProperties(prefix = "github")
@Validated
public record GithubProperties(
        @NotBlank String codeSearchUrl,
        @NotEmpty Set<String> approvedAuthors,
        int singleRequestMaxOr,
        int codeSearchPerPage
) {
    @ConstructorBinding
    public GithubProperties {
    }
}
