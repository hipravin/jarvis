package hipravin.jarvis.googlebooks.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hipravin.jarvis.github.jackson.JacksonUtils;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolumes;

public class JacksonGoogleBooksMapperImpl implements JacksonGoogleBooksMapper {
    private final ObjectMapper objectMapper = JacksonUtils.createGithubObjectMapper();

    @Override
    public BooksVolumes readBooksVolumes(String contentJson) {
        try {
            return objectMapper.readValue(contentJson, BooksVolumes.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
