package hipravin.jarvis.github.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import org.springframework.stereotype.Component;

@Component
public class JacksonGithubMapperImpl implements JacksonGithubMapper {
    private final ObjectMapper objectMapper = JacksonUtils.createGithubObjectMapper();

    @Override
    public CodeSearchResult readCodeSearchResult(String content) {
        try {
            return objectMapper.readValue(content, CodeSearchResult.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
