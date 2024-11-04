package hipravin.jarvis.github.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @deprecated replaced with TextMatch
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated
public record Match(
        @JsonProperty("indices") List<Integer> indices,
        @JsonProperty("text") String text
) {
}
