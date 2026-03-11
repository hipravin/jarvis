package hipravin.jarvis.stackexchange.client;

import hipravin.jarvis.stackexchange.client.dto.ResponseItems;
import hipravin.jarvis.stackexchange.client.dto.SearchExcerpt;

public interface StackExchangeApiClient {
    ResponseItems<SearchExcerpt> searchExcerpts(String query);

}
