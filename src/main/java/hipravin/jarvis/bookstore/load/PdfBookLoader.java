package hipravin.jarvis.bookstore.load;

import hipravin.jarvis.bookstore.load.model.Book;
import hipravin.jarvis.bookstore.load.model.BookPage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PdfBookLoader implements BookLoader {
    @Override
    public Book load(Path pdfFilePath) {
        try (PDDocument document = Loader.loadPDF(pdfFilePath.toFile())) {
            AccessPermission ap = document.getCurrentAccessPermission();
            if (!ap.canExtractContent()) {
                throw new PdfProcessException("Extract content is forbidden for file '%s', access permission: %d"
                        .formatted(pdfFilePath, ap.getPermissionBytes()));
            }

            List<BookPage> pages = new ArrayList<>();
            processPdfPages(document, (pageNum, pageContent) -> {
                pages.add(new BookPage(pageNum, pageContent));
            });

            return new Book(pdfFilePath.toString(), pdfFilePath.getFileName().toString(), pages);
        } catch (IOException e) {
            throw new PdfProcessException(e.getMessage(), e);
        }
    }

    private void processPdfPages(PDDocument document, BiConsumer<Integer, String> pageConsumer) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(false);

        for (int i = 1; i <= document.getNumberOfPages(); i++) {
            stripper.setStartPage(i);
            stripper.setEndPage(i);

            String text = stripper.getText(document);

            pageConsumer.accept(i - 1, text);//adjust to 0-based
        }
    }
}
