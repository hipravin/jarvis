package hipravin.jarvis.bookstore.load;

import jdk.jfr.*;

import java.nio.file.Path;
import java.util.Optional;

@Name(BookLoadEvent.NAME)
@Category({"Jarvis", "Bookstore"})
@StackTrace(false)
public class BookLoadEvent extends Event {
    public static final String NAME = "bookstore.BookLoadEvent";

    @Label("Path")
    private String path;

    @Label("Success")
    private Boolean success = false;

    @Label("ExceptionMessage")
    private String exceptionMessage;

    public static BookLoadEvent begin(Path bookPdf) {
        BookLoadEvent event = new BookLoadEvent();
        if (!event.isEnabled()) {
            return null;
        }
        event.path = String.valueOf(bookPdf);
        event.begin();

        return event;
    }

    public static void commitSuccess(BookLoadEvent event) {
        if (event == null || !event.isEnabled() || !event.shouldCommit()) {
            return;
        }
        event.success = true;
        event.commit();
    }

    public static void commitException(BookLoadEvent event, Throwable t) {
        if (event == null || !event.isEnabled() || !event.shouldCommit()) {
            return;
        }

        event.exceptionMessage = Optional.ofNullable(t.getMessage())
                .map(m -> t.getClass().getName() + ": " + m.substring(0, Math.min(m.length(), 1000)))
                .orElse(t.getClass().getName());
        event.commit();
    }
}