package hipravin.jarvis.github;

import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.SearchService;
import hipravin.jarvis.enginev2.dto.SearchRequest;
import hipravin.jarvis.enginev2.dto.SearchResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(SearchService.Order.ORDER_1)
public class GithubSearchService implements SearchService {
    @Override
    public SearchResponse search(SearchRequest request) {
        return null;
    }

    @Override
    public InformationSource getSource() {
        return InformationSource.GITHUB;
    }
}
