package hipravin.jarvis.stackexchange.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchExcerpt(
        List<String> tags,
        @JsonProperty("question_id") Long questionId,
        @JsonProperty("answer_id") Long answerId,
        @JsonProperty("last_activity_date") Instant lastActivityDate,
        @JsonProperty("creation_date") Instant creationDate,
        @JsonProperty("item_type") String itemType,
        String title,
        String body,
        String excerpt,
        Long score
) {
}
