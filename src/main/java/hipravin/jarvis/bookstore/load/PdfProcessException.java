package hipravin.jarvis.bookstore.load;

public class PdfProcessException extends RuntimeException {
    public PdfProcessException(String message) {
        super(message);
    }

    public PdfProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
