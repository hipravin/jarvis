package hipravin.jarvis.bookstore;


import hipravin.jarvis.bookstore.dao.BookstoreDao;
import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.load.BookReader;
import hipravin.jarvis.bookstore.load.model.Book;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookstore")
@Validated
public class BookstoreController {

    private final BookstoreDao bookstoreDao;
    private final BookReader bookLoader;

    public BookstoreController(BookstoreDao bookstoreDao, BookReader bookLoader) {
        this.bookstoreDao = bookstoreDao;
        this.bookLoader = bookLoader;
    }

    @GetMapping("/manage/sample")
    public Map<String, String> sample() {
        return Map.of("response", "Hello");
    }

    @PostMapping("/manage/upload")
    public ResponseEntity<?> handleBookUpload(@RequestParam("file") MultipartFile file) throws IOException {
        Book book = bookLoader.read(file.getInputStream().readAllBytes(), file.getOriginalFilename());
        BookEntity bookEntity = bookstoreDao.save(book);

        return ResponseEntity.ok(Map.of("title", book.title(), "id", bookEntity.getId()));
    }

    @GetMapping(path ="/book/{id}/rawpdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> rawPdf(@NotNull @PathVariable("id") Long id) {
        BookEntity bookEntity = bookstoreDao.findByIdFetchPdf(id);
        return ResponseEntity.ok(bookEntity.getPdfContent());
    }
}
