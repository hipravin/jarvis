package hipravin.jarvis.github;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.List;


@ConfigurationProperties(prefix = "github")
public record GithubProperties(
        String codeSearchUrl,
        List<String> approvedAuthors,
        int singleRequestMaxOr
) {
    @ConstructorBinding
    public GithubProperties {
    }
}
