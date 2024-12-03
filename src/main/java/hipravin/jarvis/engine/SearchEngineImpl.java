package hipravin.jarvis.engine;

import hipravin.jarvis.engine.model.*;
import hipravin.jarvis.github.GithubApiClient;
import hipravin.jarvis.googlebooks.GoogleBooksApiClient;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolume;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolumes;
import org.springframework.stereotype.Service;

@Service
public class SearchEngineImpl implements SearchEngine {

    private final GithubApiClient githubApiClient;
    private final GoogleBooksApiClient googleBooksApiClient;

    public SearchEngineImpl(GithubApiClient githubApiClient, GoogleBooksApiClient googleBooksApiClient) {
        this.githubApiClient = githubApiClient;
        this.googleBooksApiClient = googleBooksApiClient;
    }

    @Override
    public JarvisResponse search(JarvisRequest request) {
        return searchGoogleBooks(request.query());
    }

    private JarvisResponse searchGoogleBooks(String query) {
        BooksVolumes booksVolumes = googleBooksApiClient.search(query);

        if(booksVolumes.items() == null) {
            return JarvisResponse.EMPTY_RESPONSE;
        }

        var codeFragments = booksVolumes.items().stream()
                .filter(BooksVolume.HAS_TEXT_SNIPPET.and(BooksVolume.HAS_PREVIEW_LINK))
                .map(bv -> new CodeFragment(bv.searchInfo().textSnippet(),
                        new Link(bv.volumeInfo().title(), bv.volumeInfo().previewLink()),
                        bv.volumeInfo().publisher()))
                .toList();

        var authors = booksVolumes.items().stream()
                .map(bv -> new AuthorResult(bv.volumeInfo().title(), 1))
                .toList();

        return new JarvisResponse("", authors, codeFragments);
    }

}
