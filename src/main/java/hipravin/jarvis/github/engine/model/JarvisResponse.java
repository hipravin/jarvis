package hipravin.jarvis.github.engine.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JarvisResponse(
        @JsonProperty("response") String response,
        @JsonProperty("code") String code
) {
}
