package hipravin.jarvis.enginev2.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchRequest(
        String query
) {
}
