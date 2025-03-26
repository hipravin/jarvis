package hipravin.jarvis.bookstore.load;

public class PdfReadException extends RuntimeException {
    public PdfReadException(String message) {
        super(message);
    }

    public PdfReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
