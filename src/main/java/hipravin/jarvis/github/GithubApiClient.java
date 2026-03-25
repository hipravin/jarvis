package hipravin.jarvis.github;

import hipravin.jarvis.github.jackson.model.CodeSearchResult;

public interface GithubApiClient {
    String getContent(String uri);
    CodeSearchResult search(String searchString);
    CodeSearchResult searchApprovedAuthors(String searchString);

    String buildUserSearchUrl(String user, String query);
}
