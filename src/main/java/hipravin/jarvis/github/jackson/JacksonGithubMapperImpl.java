package hipravin.jarvis.github.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import hipravin.jarvis.github.jackson.model.EncodedContent;
import org.springframework.stereotype.Component;

@Component
public class JacksonGithubMapperImpl implements JacksonGithubMapper {
    private final ObjectMapper objectMapper = JacksonUtils.createGithubObjectMapper();

    @Override
    public CodeSearchResult readCodeSearchResult(String contentJson) {
        try {
            return objectMapper.readValue(contentJson, CodeSearchResult.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EncodedContent readContent(String contentJson) {
        try {
            return objectMapper.readValue(contentJson, EncodedContent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
