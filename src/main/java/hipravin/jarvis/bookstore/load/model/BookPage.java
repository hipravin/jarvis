package hipravin.jarvis.bookstore.load.model;

public record BookPage(
        long pageNum,
        String content,
        byte[] pdfContent
) {
}
