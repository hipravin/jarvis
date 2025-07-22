package hipravin.jarvis.bookstore;

import hipravin.jarvis.bookstore.dao.BookstoreDao;
import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.load.BookReader;
import hipravin.jarvis.bookstore.load.BookstoreProperties;
import hipravin.jarvis.bookstore.load.DirectoryUtil;
import hipravin.jarvis.bookstore.load.PdfBookReader;
import hipravin.jarvis.bookstore.load.model.Book;
import jdk.jfr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        Map<String, BookEntity> existingBooksByTitle = bookstoreDao.findAll().stream()
                .collect(Collectors.toMap(BookEntity::getTitle, Function.identity()));

        List<Path> pdfFiles = DirectoryUtil.findFilesRecursively(bookstoreProperties.loaderRootPath(), "pdf");

        for (Path pdfFile : pdfFiles) {
            //we don't know actual title until we parse pdf which is large and wish to avoid
            //but most likely title is just filename with '.pdf' extension removed
            //ugly design, but such tactical solution might be good enough
            //we are not paid for perfection yet
            //strategic solution would be to introduce loader's own table with processing statuses for each file
            //such table could be used also to prevent concurrent file loading if multiple loader instances are spawned
            String probableFileTitle = PdfBookReader.removePdfExtension(pdfFile.getFileName().toString());
            var existingBook = existingBooksByTitle.get(probableFileTitle);
            if(existingBook != null) {
                try {
                    if(existingBook.getLastUpdated() != null
                             && existingBook.getLastUpdated().isAfter(Files.getLastModifiedTime(pdfFile).toInstant())) {
                        log.info("skipping book '{}': already loaded", pdfFile);
                        continue;
                    }
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                }
            }
            readAndSaveNewBook(pdfFile);
        }
    }

    //File system watch does not work with mounted volumes (https://forums.docker.com/t/file-system-watch-does-not-work-with-mounted-volumes/12038)
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
