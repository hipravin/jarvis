package hipravin.jarvis.github.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CodeSearchItem(
        @JsonProperty("name") String name,
        @JsonProperty("path") String path,
        @JsonProperty("url") String url,
        @JsonProperty("sha") String sha,
        @JsonProperty("git_url") String gitUrl,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("repository") Repository repository,
        @JsonProperty("score") double score,
        @JsonProperty("text_matches") List<TextMatches> textMatches
) {
}
