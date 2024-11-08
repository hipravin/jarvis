package hipravin.jarvis.github.jackson;

import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import hipravin.jarvis.github.jackson.model.EncodedContent;

public interface JacksonGithubMapper {
    CodeSearchResult readCodeSearchResult(String contentJson);
    EncodedContent readContent(String contentJson);
}
