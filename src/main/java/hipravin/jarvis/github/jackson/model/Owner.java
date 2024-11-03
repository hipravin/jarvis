package hipravin.jarvis.github.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Owner(
        @JsonProperty("login") String login,
        @JsonProperty("url") String url,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("type") String type
) {
}
