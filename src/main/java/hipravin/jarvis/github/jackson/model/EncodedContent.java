package hipravin.jarvis.github.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EncodedContent(
        @JsonProperty("sha") String sha,
        @JsonProperty("url") String url,
        @JsonProperty("size") Integer size,
        @JsonProperty("node_id") String nodeId,
        @JsonProperty("content") String content,
        @JsonProperty("encoding") String encoding
) {
}
