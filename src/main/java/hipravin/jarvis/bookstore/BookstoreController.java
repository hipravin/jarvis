package hipravin.jarvis.bookstore;


import hipravin.jarvis.bookstore.dao.BookstoreDao;
import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.load.BookReader;
import hipravin.jarvis.bookstore.load.model.Book;
import hipravin.jarvis.exception.NotFoundException;
import hipravin.jarvis.openapi.TagGetBookContent;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookstore")
@Validated
public class BookstoreController {

    private final BookstoreDao bookstoreDao;
    private final BookReader bookReader;

    public BookstoreController(BookstoreDao bookstoreDao, BookReader bookLoader) {
        this.bookstoreDao = bookstoreDao;
        this.bookReader = bookLoader;
    }

    @PostMapping("/manage/upload")
    public ResponseEntity<?> handleBookUpload(@RequestParam("file") MultipartFile file) throws IOException {
        Book book = bookReader.read(file.getInputStream().readAllBytes(), file.getOriginalFilename());
        BookEntity bookEntity = bookstoreDao.save(book);

        return ResponseEntity.ok(Map.of("title", book.title(), "id", bookEntity.getId()));
    }

    @TagGetBookContent
    @GetMapping(path = "/book/{id}/rawpdfjpa", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<byte[]> rawPdf(@NotNull @PathVariable("id") Long id) {
        BookEntity bookEntity = bookstoreDao.findByIdFetchPdf(id);
        return ResponseEntity.ok(bookEntity.getPdfContent());
    }

    @TagGetBookContent
    @GetMapping(path = "/book/{id}/rawpdf", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<StreamingResponseBody> rawPdfStreaming(@NotNull @PathVariable("id") Long id) {
        StreamingResponseBody responseBody = out -> {
            bookstoreDao.writePdfContentTo(id, out);
        };

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

        return new ResponseEntity<>(responseBody, responseHeaders, HttpStatus.OK);
    }
}
