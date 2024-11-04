package hipravin.jarvis.github;

import hipravin.jarvis.github.jackson.model.CodeSearchResult;

public interface GithubApiClient {
    CodeSearchResult search(String searchString);
    CodeSearchResult searchApprovedAuthors(String searchString);

}
