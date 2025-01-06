package hipravin.jarvis.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JarvisRequest(
        @JsonProperty("query") @NotBlank String query,
        @JsonProperty("providers") Set<SearchProviderType> searchProviders
) {
}
