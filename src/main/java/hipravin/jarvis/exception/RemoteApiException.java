package hipravin.jarvis.exception;

import java.io.Serial;

public class RemoteApiException extends RuntimeException {
    @Serial
    static final long serialVersionUID = 1L;

    public RemoteApiException() {
    }

    public RemoteApiException(String message) {
        super(message);
    }

    public RemoteApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
