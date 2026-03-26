package hipravin.jarvis.stackexchange.client;

import hipravin.jarvis.stackexchange.client.dto.ResponseItems;
import hipravin.jarvis.stackexchange.client.dto.SearchExcerpt;

public interface JsonReader {
    ResponseItems<SearchExcerpt> readSearchExcerpts(String json);
}
