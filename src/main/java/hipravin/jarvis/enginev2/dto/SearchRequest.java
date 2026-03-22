package hipravin.jarvis.enginev2.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import hipravin.jarvis.engine.model.InformationSource;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchRequest(
        @JsonProperty("query") @NotBlank String query,
        @JsonProperty("providers") Set<InformationSource> searchProviders) {
}
