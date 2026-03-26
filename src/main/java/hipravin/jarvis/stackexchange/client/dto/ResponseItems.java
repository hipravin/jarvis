package hipravin.jarvis.stackexchange.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResponseItems<T>(
        List<T> items,
        @JsonProperty("has_more") Boolean hasMore,
        @JsonProperty("quota_max") Long quotaMax,
        @JsonProperty("quota_remaining") Long quotaRemaining
) {
}
