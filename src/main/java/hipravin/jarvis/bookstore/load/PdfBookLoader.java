package hipravin.jarvis.bookstore.load;

import hipravin.jarvis.bookstore.load.model.Book;
import hipravin.jarvis.bookstore.load.model.BookMetadata;
import hipravin.jarvis.bookstore.load.model.BookPage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

@Component
public class PdfBookLoader implements BookLoader {

    @Override
    public Book load(Path pdfFilePath) {
        try {
            requirePdf(pdfFilePath);

            byte[] docBytes = Files.readAllBytes(pdfFilePath);
            return load(docBytes, pdfFilePath.getFileName().toString());
        } catch (IOException e) {
            throw new PdfProcessException(e.getMessage(), e);
        }
    }

    @Override
    public Book load(byte[] documentBinaryContent, String title) {
        try (PDDocument document = Loader.loadPDF(documentBinaryContent)) {
            AccessPermission ap = document.getCurrentAccessPermission();
            if (!ap.canExtractContent()) {
                throw new PdfProcessException("Extract content is forbidden for document '%s', access permission: %d"
                        .formatted(title, ap.getPermissionBytes()));
            }

            List<BookPage> pages = new ArrayList<>();
            processPdfPages(document, (num, content) -> {
                pages.add(new BookPage(num, content, extractPdfPage(documentBinaryContent, num)));
            });

            return new Book(
                    removePdfExtension(title),
                    null,
                    null,
                    BookMetadata.from(document.getDocumentInformation()),
                    pages, documentBinaryContent);
        } catch (IOException e) {
            throw new PdfProcessException(e.getMessage(), e);
        }
    }

    private static Path requirePdf(Path path) throws IOException {
        if (path == null || !Files.isReadable(path)) {
            throw new PdfProcessException(path + " is not a readable file");
        }
        String contentType = Files.probeContentType(path);
        if (!"application/pdf".equals(contentType)) {
            throw new PdfProcessException("File is not in pdf format: " + path + ", actual content type: " + contentType);
        }
        return path;
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

    private static String removePdfExtension(String filename) {
        if (filename.toLowerCase().endsWith(".pdf")) {
            return filename.substring(0, filename.length() - ".pdf".length());
        } else {
            return filename;
        }
    }

    private byte[] extractPdfPage(byte[] documentBytes, int pageNum) {
        //naive implementation extractPdfPageNotOptimized produces result where
        //size of single page pdf is almost as big as whole document.
        //turned off until optimized. As of now single page pdf will be constructed from whole document on the fly.
        //possible solution: https://stackoverflow.com/questions/51642943/cleaning-up-unused-images-in-pdf-page-resources
        return null;
    }

    /**
     * @param pageNum 0-based
     */
    private byte[] extractPdfPageNotOptimized(byte[] documentBytes, int pageNum) {
        byte[] documentToModify = Arrays.copyOf(documentBytes, documentBytes.length);
        try (PDDocument document = Loader.loadPDF(documentToModify)) {
            for (int i = 0; i < pageNum; i++) {
                document.removePage(0);
            }
            while (document.getNumberOfPages() > 1) {
                document.removePage(document.getNumberOfPages() - 1);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            document.save(bos);

            return bos.toByteArray();
        } catch (IOException e) {
            throw new PdfProcessException(e.getMessage(), e);
        }
    }
}
