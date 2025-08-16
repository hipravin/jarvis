package hipravin.jarvis.exception;

import java.io.Serial;

public class RateLimitExceedException extends RemoteApiException {
    @Serial
    static final long serialVersionUID = 1L;

    public RateLimitExceedException() {
    }

    public RateLimitExceedException(String message) {
        super(message);
    }

    public RateLimitExceedException(String message, Throwable cause) {
        super(message, cause);
    }
}
