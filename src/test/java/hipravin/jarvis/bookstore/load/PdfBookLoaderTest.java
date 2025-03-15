package hipravin.jarvis.bookstore.load;

import hipravin.jarvis.bookstore.load.model.Book;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PdfBookLoaderTest {
    static Path sampleGarlicPdf = Path.of("src/test/resources/data/bookstore/garlic-onion-15.JChromat.A2006.pdf");
    static Path sampleSaltPdf = Path.of("src/test/resources/data/bookstore/estimating salt intake not so easy.pdf");

    @Test
    void loadSample() {
        PdfBookLoader loader = new PdfBookLoader();

        Book garlicOnion = loader.load(sampleSaltPdf);

        garlicOnion.pages().forEach(p -> System.out.printf("%d%n%s%n%n%n", p.pageNum(), p.content()));
    }
}