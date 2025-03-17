package hipravin.jarvis.bookstore.load;

import hipravin.jarvis.bookstore.load.model.Book;
import hipravin.jarvis.bookstore.load.model.BookPage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PdfBookLoaderTest {
    static Path sampleGarlicPdf = Path.of("src/test/resources/data/bookstore/garlic-onion-15.JChromat.A2006.pdf");
    static Path sampleSaltPdf = Path.of("src/test/resources/data/bookstore/estimating salt intake not so easy.pdf");
    static Path sampleStarchPdf = Path.of("src/test/resources/data/bookstore/Hardy_QRB15_starch.pdf");

    static PdfBookLoader loader = new PdfBookLoader();

    @Test
    void testLoadSalt() {
        Book sb = loader.load(sampleSaltPdf);

        assertEquals("estimating salt intake not so easy", sb.source());
        assertEquals("Estimating salt intake in humans: not so easy!1", sb.metadata().title());
        assertEquals("Titze Jens", sb.metadata().author());
        assertEquals(OffsetDateTime.parse("2023-02-10T10:18:28+05:30"), sb.metadata().creationDate());

        assertEquals(9, sb.metadata().metadata().size());
        assertEquals("10.3945/ajcn.117.158147", sb.metadata().metadata().get("doi"));
    }

    @Test
    void testFewPdfload() throws IOException {
        Book b1 = loader.load(sampleSaltPdf);
        Book b2 = loader.load(sampleStarchPdf);
        Book b3 = loader.load(sampleGarlicPdf);

        assertEquals("Estimating salt intake in humans: not so easy!1", b1.metadata().title());
        assertEquals("untitled", b2.metadata().title());
        assertEquals("doi:10.1016/j.chroma.2005.12.016", b3.metadata().title());

        ensureParsedCorrectly(b1);
        ensureParsedCorrectly(b2);
        ensureParsedCorrectly(b3);
    }

    void ensureParsedCorrectly(Book book) throws IOException {
        assertNotNull(book);
        assertNotNull(book.pdfContent());

        for (BookPage page : book.pages()) {
            try(PDDocument pageDocument = Loader.loadPDF(page.pdfContent())) {
                assertEquals(1, pageDocument.getNumberOfPages());

                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(1);
                stripper.setEndPage(1);
                String parsedContent = stripper.getText(pageDocument);

                assertEquals(page.content(), parsedContent);
            }
        }
    }
}