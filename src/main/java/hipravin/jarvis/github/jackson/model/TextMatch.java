package hipravin.jarvis.github.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TextMatch(
        @JsonProperty("object_url") String objectUrl,
        @JsonProperty("object_type") String objectType,
        @JsonProperty("property") String property,
        @JsonProperty("fragment") String fragment,
        @JsonProperty("matches") List<Match> matches
) {
}
