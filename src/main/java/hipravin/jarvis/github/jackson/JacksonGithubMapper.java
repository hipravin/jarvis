package hipravin.jarvis.github.jackson;

import hipravin.jarvis.github.jackson.model.CodeSearchResult;

public interface JacksonGithubMapper {
    CodeSearchResult readCodeSearchResult(String content);
}
