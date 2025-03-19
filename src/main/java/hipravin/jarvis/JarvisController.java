package hipravin.jarvis;

import hipravin.jarvis.bookstore.dao.BookstoreDao;
import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.engine.SearchEngine;
import hipravin.jarvis.engine.model.JarvisRequest;
import hipravin.jarvis.engine.model.JarvisResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping(path = "/api/v1/jarvis")
public class JarvisController {

    private final SearchEngine searchEngine;
    private final BookstoreDao bookstoreDao;

    public JarvisController(SearchEngine searchEngine, BookstoreDao bookstoreDao) {
        this.searchEngine = searchEngine;
        this.bookstoreDao = bookstoreDao;
    }

    @PostMapping("/query")
    public ResponseEntity<JarvisResponse> query(@Valid @RequestBody JarvisRequest request) {
        JarvisResponse response = searchEngine.search(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping(path ="/book/{id}/rawpdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> rawPdf(@NotNull @PathVariable("id") Long id) {
        BookEntity bookEntity = bookstoreDao.findById(id);
        return ResponseEntity.ok(bookEntity.getPdfContent());
    }
}
