package hipravin.jarvis.exception;

public class BookNotFoundException extends NotFoundException {

    public BookNotFoundException(Long id) {
        super("Book not found: %s".formatted(String.valueOf(id)));
    }
}
