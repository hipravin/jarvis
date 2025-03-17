package hipravin.jarvis.bookstore.load.model;

import java.util.List;

public record Book(
        String source,
        BookMetadata metadata,
        List<BookPage> pages,
        byte[] pdfContent
) {
}
