package hipravin.jarvis.exception;

import java.io.Serial;

public class BadHeaderValueException extends Exception {
    @Serial
    static final long serialVersionUID = 1L;

    private final String headerName;

    public BadHeaderValueException(String headerName, String message) {
        super(message);
        this.headerName = headerName;
    }

    public String getHeaderName() {
        return headerName;
    }
}
