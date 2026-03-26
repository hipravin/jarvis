package hipravin.jarvis.googlebooks;

import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.SearchService;
import hipravin.jarvis.enginev2.dto.*;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolume;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolumes;
import hipravin.jarvis.googlebooks.jackson.model.SearchInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@Order(SearchService.Order.ORDER_4)
public class GoogleBooksSearchService implements SearchService{
    private static final Logger log = LoggerFactory.getLogger(GoogleBooksSearchService.class);

    private final GoogleBooksApiClient googleBooksApiClient;

    public GoogleBooksSearchService(GoogleBooksApiClient googleBooksApiClient) {
        this.googleBooksApiClient = googleBooksApiClient;
    }

    @Override
    public SearchResponse search(SearchRequest request) {
        try {
            return SearchResponse.success(search(request.query()));
        } catch(RuntimeException e) {
            log.error("Search failed for query '{}': {}", request, e.getMessage(), e);
            return SearchResponse.failed(e);
        }
    }

    private List<Excerpt> search(String query) {
        BooksVolumes booksVolumes = googleBooksApiClient.search(query);

        if (booksVolumes.items() == null || booksVolumes.items().isEmpty()) {
            return List.of();
        }

        Function<BooksVolume, Link> volumeToLink = (bv) ->
                new Link(bv.volumeInfo().title() + ", " +
                        bv.yearPublished().orElse("n/a"),
                        bv.volumeInfo().previewLink());

        Function<BooksVolume, String> volumeToSnippet = (bv) ->
                Optional.ofNullable(bv.searchInfo()).map(SearchInfo::textSnippet).orElse("n/a");

        return booksVolumes.items().stream()
                .map(this::toExcerpt)
                .toList();
    }

    private Excerpt toExcerpt(BooksVolume bv) {
        return Excerpt.builder()
                .source(InformationSource.GOOGLE_BOOKS)
                .title(bookTitle(bv), bookUrl(bv))
                .mainHtml(snippet(bv))
                .build();
    }

    private String bookTitle(BooksVolume bv) {
        return bv.volumeInfo().title() + ", " + bv.yearPublished().orElse("n/a");
    }

    private String bookUrl(BooksVolume bv) {
        return bv.volumeInfo().previewLink();
    }

    private String snippet(BooksVolume bv) {
        return (bv.searchInfo() != null)
                ? bv.searchInfo().textSnippet()
                : "n/a";
    }

    @Override
    public InformationSource getSource() {
        return InformationSource.GOOGLE_BOOKS;
    }
}
