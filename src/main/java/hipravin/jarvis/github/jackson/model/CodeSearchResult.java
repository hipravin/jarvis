package hipravin.jarvis.github.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CodeSearchResult(
   @JsonProperty("total_count") Integer count,
   @JsonProperty("incomplete_results") Boolean incompleteResults,
   @JsonProperty("items") List<CodeSearchItem> codeSearchItems
) {
}
