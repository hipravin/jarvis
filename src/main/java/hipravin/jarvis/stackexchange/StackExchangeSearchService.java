package hipravin.jarvis.stackexchange;

import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.SearchService;
import hipravin.jarvis.enginev2.dto.*;
import hipravin.jarvis.stackexchange.client.StackExchangeApiClient;
import hipravin.jarvis.stackexchange.client.dto.ResponseItems;
import hipravin.jarvis.stackexchange.client.dto.SearchExcerpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Order(SearchService.Order.ORDER_3)
public class StackExchangeSearchService implements SearchService {
    private static final Logger log = LoggerFactory.getLogger(StackExchangeSearchService.class);

    private final StackExchangeApiClient apiClient;

    public StackExchangeSearchService(StackExchangeApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public SearchResponse search(SearchRequest request) {
        try {
            ResponseItems<SearchExcerpt> excerpts = apiClient.searchExcerpts(request.query());

            List<Excerpt> excerptsDto = excerpts.items().stream()
                    .map(this::mapToDto)
                    .toList();

            return SearchResponse.success(excerptsDto);
        } catch (RuntimeException e) {
            log.error("Search failed for query '{}': {}", request, e.getMessage(), e);
            return SearchResponse.failed(e);
        }
    }

    public Excerpt mapToDto(SearchExcerpt searchExcerpt) {
        return Excerpt.builder()
                .source(InformationSource.STACKEXCHANGE)
                .title(searchExcerpt.title(), stackOverflowUrl(searchExcerpt))
                .mainHtml(collapseAdjacentNewlines(searchExcerpt.excerpt()))
                .build();
    }

    String collapseAdjacentNewlines(String text) {
        return text.replaceAll("\\R{2,}", "\n\n");
    }

    String stackOverflowUrl(SearchExcerpt se) {
        return (se.questionId() == null)
                ? "about:blank"
                : "https://stackoverflow.com/questions/%d/".formatted(se.questionId());
    }

    @Override
    public InformationSource getSource() {
        return InformationSource.STACKEXCHANGE;
    }
}
