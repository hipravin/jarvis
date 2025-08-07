package hipravin.jarvis.exception;

import java.io.Serial;

public class NotFoundException extends RuntimeException {
    @Serial
    static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
