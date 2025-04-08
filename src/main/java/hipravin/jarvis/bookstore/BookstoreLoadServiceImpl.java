package hipravin.jarvis.bookstore;

import hipravin.jarvis.bookstore.dao.BookstoreDao;
import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.load.BookReader;
import hipravin.jarvis.bookstore.load.BookstoreProperties;
import hipravin.jarvis.bookstore.load.DirectoryUtil;
import hipravin.jarvis.bookstore.load.model.Book;
import jdk.jfr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Component
public class BookstoreLoadServiceImpl implements BookstoreLoadService {
    private static final Logger log = LoggerFactory.getLogger(BookstoreLoadServiceImpl.class);

    private final BookstoreProperties bookstoreProperties;
    private final BookReader bookReader;
    private final BookstoreDao bookstoreDao;

    public BookstoreLoadServiceImpl(BookstoreProperties bookstoreProperties, BookReader bookReader, BookstoreDao bookstoreDao) {
        this.bookstoreProperties = bookstoreProperties;
        this.bookReader = bookReader;
        this.bookstoreDao = bookstoreDao;
    }

    @Override
    public void loadAll() {
        List<Path> pdfFiles = DirectoryUtil.findFilesRecursively(bookstoreProperties.loaderRootPath(), "pdf");

        for (Path pdfFile : pdfFiles) {
            readAndSaveNewBook(pdfFile);
        }
    }

    @Override
    public void handleUpdate(DirectoryUtil.ChangeEvent directoryChangeEvent) {
        log.info("Processing storage update {}", directoryChangeEvent);
        switch (directoryChangeEvent.kind()) {
            case CREATE -> {
                readAndSaveNewBook(directoryChangeEvent.path());
            }
            case DELETE, MODIFY -> {
                log.info("ignored");
            }
        }
    }

    private void readAndSaveNewBook(Path bookPdf) {
        BookLoadEvent jfr = BookLoadEvent.begin(bookPdf);
        try {
            Book book = bookReader.read(bookPdf);
            BookEntity bookEntity = bookstoreDao.save(book);
            log.debug("New book saved: id: {}, pages: {}, title: {}", bookEntity.getId(), book.pages().size(), bookEntity.getTitle());
            jfr.commitSuccess();
        } catch (RuntimeException e) {
            log.error("Failed to load pdf file: {}", e.getMessage(), e);
            jfr.commitException(e);
        }
    }

    @Name("BookLoad")
    @Category({"Jarvis", "Bookstore"})
    @StackTrace(false)
    static class BookLoadEvent extends Event {
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
}
