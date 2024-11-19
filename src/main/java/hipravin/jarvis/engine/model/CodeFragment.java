package hipravin.jarvis.engine.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CodeFragment(
        @JsonProperty("code") String code,
        @JsonProperty("link") Link link,
        @JsonProperty("author") String author
) {
}
