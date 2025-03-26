package hipravin.jarvis.bookstore.load.model;

import java.util.List;

/**
 *
 * @param title Unique not null value presented to end user
 * @param firstPublished Year when first edition of the book was published
 * @param editionPublished Year when current edition of the book was published
 * @param metadata
 * @param pages
 * @param pdfContent
 */
public record Book(
        String title,
        Integer firstPublished,
        Integer editionPublished,
        BookMetadata metadata,
        List<BookPage> pages,
        byte[] pdfContent
) {
}
