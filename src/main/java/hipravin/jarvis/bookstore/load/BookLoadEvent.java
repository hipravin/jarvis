package hipravin.jarvis.bookstore.load;

import jdk.jfr.*;

import java.nio.file.Path;
import java.util.Optional;

@Name("BookLoad")
@Category({"Jarvis", "Bookstore"})
@StackTrace(false)
public class BookLoadEvent extends Event {
    @Label("Path")
    private String path;

    @Label("Success")
    private Boolean success = false;

    @Label("ExceptionMessage")
    private String exceptionMessage;

    public static BookLoadEvent begin(Path bookPdf) {
        BookLoadEvent event = new BookLoadEvent();
        event.path = String.valueOf(bookPdf);
        event.begin();

        return event;
    }

    public void commitSuccess() {
        success = true;
        end();
        commit();
    }

    public void commitException(Throwable t) {
        exceptionMessage = Optional.ofNullable(t.getMessage())
                .map(m -> t.getClass().getName() + ": " + m.substring(0, Math.min(m.length(), 1000)))
                .orElse(t.getClass().getName());
        end();
        commit();
    }
}