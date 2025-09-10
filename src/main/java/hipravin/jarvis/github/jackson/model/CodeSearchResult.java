package hipravin.jarvis.github.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import hipravin.jarvis.github.GithubUtils;

import java.util.*;

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

    public CodeSearchResult sort(Comparator<? super CodeSearchItem> itemComparator) {
        List<CodeSearchItem> items = new ArrayList<>(this.codeSearchItems());
        items.sort(itemComparator);

        return new CodeSearchResult(this.count(), this.incompleteResults(), items);
    }

    public CodeSearchResult sort(List<String> authorsOrdered) {
        Map<String, Integer> authorToPosition = new HashMap<>();
        int position = 0;
        for (String author : authorsOrdered) {
            authorToPosition.put(author, position++);
        }

        Comparator<CodeSearchItem> byAuthorPosition = Comparator.comparing(item ->
                authorToPosition.getOrDefault(GithubUtils.safeGetLogin(item), Integer.MAX_VALUE));

        return this.sort(byAuthorPosition);
    }
}
