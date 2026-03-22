package hipravin.jarvis.stackexchange;

import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.SearchService;
import hipravin.jarvis.enginev2.dto.Excerpt;
import hipravin.jarvis.enginev2.dto.SearchRequest;
import hipravin.jarvis.enginev2.dto.SearchResponse;
import hipravin.jarvis.stackexchange.client.StackExchangeApiClient;
import hipravin.jarvis.stackexchange.client.dto.ResponseItems;
import hipravin.jarvis.stackexchange.client.dto.SearchExcerpt;
import hipravin.jarvis.stackexchange.mapper.StackExchangeDtoMapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Order(SearchService.Order.ORDER_3)
public class StackExchangeSearchService implements SearchService {

    private final StackExchangeApiClient apiClient;
    private final StackExchangeDtoMapper mapper;

    public StackExchangeSearchService(StackExchangeApiClient apiClient,
                                      StackExchangeDtoMapper mapper) {
        this.apiClient = apiClient;
        this.mapper = mapper;
    }

    @Override
    public SearchResponse search(SearchRequest request) {
        try {
            ResponseItems<SearchExcerpt> excerpts = apiClient.searchExcerpts(request.query());

            List<Excerpt> excerptsDto = excerpts.items().stream()
                    .map(mapper::mapToDto)
                    .toList();

            return SearchResponse.success(excerptsDto);
        } catch(RuntimeException e) {
            return SearchResponse.failed(e);
        }
    }

    @Override
    public InformationSource getSource() {
        return InformationSource.STACKEXCHANGE;
    }
}
