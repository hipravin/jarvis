package hipravin.jarvis.googlebooks.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchInfo(
        String textSnippet
) {
}
