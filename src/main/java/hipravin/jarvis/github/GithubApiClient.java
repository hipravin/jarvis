package hipravin.jarvis.github;

import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import hipravin.jarvis.github.jackson.model.EncodedContent;

public interface GithubApiClient {
    String getContent(String uri);
    CodeSearchResult search(String searchString);
    CodeSearchResult searchApprovedAuthors(String searchString);
}
