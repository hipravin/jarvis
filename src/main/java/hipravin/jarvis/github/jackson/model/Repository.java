package hipravin.jarvis.github.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Repository(
        @JsonProperty("id") Long id,
        @JsonProperty("name") String name,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("owner") Owner owner,
        @JsonProperty("url") String url,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("description") String description,
        @JsonProperty("score") Double score
) {
}
