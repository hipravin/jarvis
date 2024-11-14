package hipravin.jarvis.github.engine.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JarvisRequest(
       @JsonProperty("query") String query
) {
}
