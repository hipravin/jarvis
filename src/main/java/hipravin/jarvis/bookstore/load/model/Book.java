package hipravin.jarvis.bookstore.load.model;

import java.util.List;

public record Book(
        String source,
        String title,
        List<BookPage> pages
) {
}
