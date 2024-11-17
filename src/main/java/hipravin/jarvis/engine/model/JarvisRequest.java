package hipravin.jarvis.engine.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record JarvisRequest(
       @JsonProperty("query") @NotBlank String query
) {
}
