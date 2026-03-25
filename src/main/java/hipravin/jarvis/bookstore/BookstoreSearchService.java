package hipravin.jarvis.bookstore;

import hipravin.jarvis.bookstore.dao.BookstoreDao;
import hipravin.jarvis.bookstore.dao.entity.BookPageFtsEntity;
import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.SearchService;
import hipravin.jarvis.enginev2.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Order(SearchService.Order.ORDER_2)
public class BookstoreSearchService implements SearchService {
    private static Logger log = LoggerFactory.getLogger(BookstoreSearchService.class);

    private final BookstoreDao bookstoreDao;

    public BookstoreSearchService(BookstoreDao bookstoreDao) {
        this.bookstoreDao = bookstoreDao;
    }

    @Override
    public SearchResponse search(SearchRequest request) {
        try {
            return searchBookstore(request.query());
        } catch (RuntimeException e) {
            log.error("Search failed for query '{}': {}", request, e.getMessage(), e);
            return SearchResponse.failed(e);
        }
    }

    private SearchResponse searchBookstore(String query) {
        List<BookPageFtsEntity> matchedBookPages = bookstoreDao.search(query);

        List<Excerpt> excerpts = matchedBookPages.stream()
                .map(this::fromBookPage)
                .toList();

        return SearchResponse.success(excerpts);
    }

    private Excerpt fromBookPage(BookPageFtsEntity bp) {
        String title = bp.getBook().getTitle();
        Long bookId = Objects.requireNonNull(bp.getBookPageId().bookId());
        Long pageNum = Objects.requireNonNull(bp.getBookPageId().pageNum());

        String linkText = "%s, p.%d".formatted(title, pageNum);
        String linkHref = "/api/v1/bookstore/book/%d/rawpdf#page=%d".formatted(
                bookId, pageNum);

        return Excerpt.builder()
                .source(InformationSource.BOOKSTORE)
                .title(linkText, linkHref)
                .mainHtml(bp.getContentHighlighted())
                .build();
    }

    @Override
    public InformationSource getSource() {
        return InformationSource.BOOKSTORE;
    }
}
