package hipravin.jarvis.github.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CodeSearchResult(
   @JsonProperty("total_count") Integer count,
   @JsonProperty("incomplete_results") Boolean incompleteResults,
   @JsonProperty("items") List<CodeSearchItem> codeSearchItems
) {
    public static CodeSearchResult combine(List<CodeSearchResult> results) {
        return combine(results.toArray(new CodeSearchResult[]{}));
    }

    public static CodeSearchResult combine(CodeSearchResult... codeSearchResults) {
        if(codeSearchResults.length == 0) {
            throw new IllegalArgumentException("empty array");
        }

        boolean incomplete = false;
        List<CodeSearchItem> combinedItems = new ArrayList<>();

        for (CodeSearchResult csr : codeSearchResults) {
            incomplete = incomplete || csr.incompleteResults();
            combinedItems.addAll(csr.codeSearchItems);
        }
        //TODO: sort combinedItems?
        return new CodeSearchResult(combinedItems.size(), incomplete, combinedItems);
    }
}
