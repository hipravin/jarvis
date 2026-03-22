package hipravin.jarvis.stackexchange.client;

import hipravin.jarvis.stackexchange.client.dto.ResponseItems;
import hipravin.jarvis.stackexchange.client.dto.SearchExcerpt;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
public class StackExchangeJsonReaderImpl implements JsonReader {
    private final JsonMapper jsonMapper = configureJsonMapper();

    @Override
    public ResponseItems<SearchExcerpt> readSearchExcerpts(String json) {
        return jsonMapper.readValue(json, new TypeReference<>() {});
    }

    private JsonMapper configureJsonMapper() {
        return JsonMapper.builder()
                .build();
    }
}
