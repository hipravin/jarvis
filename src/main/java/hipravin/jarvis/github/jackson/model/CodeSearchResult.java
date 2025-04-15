package hipravin.jarvis.github.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        if (codeSearchResults.length == 0) {
            throw new IllegalArgumentException("empty array");
        }

        boolean incomplete = false;
        int totalCount = 0;
        List<CodeSearchItem> combinedItems = new ArrayList<>();

        Set<String> urls = new HashSet<>();//to avoid duplicates
        for (CodeSearchResult csr : codeSearchResults) {
            incomplete = incomplete || csr.incompleteResults();
            totalCount += csr.count();
            csr.codeSearchItems.stream()
                    .filter(item -> !urls.contains(item.url()))
                    .forEach(combinedItems::add);

            csr.codeSearchItems.forEach(item -> urls.add(item.url()));
        }

        return new CodeSearchResult(totalCount, incomplete, combinedItems);
    }
}
