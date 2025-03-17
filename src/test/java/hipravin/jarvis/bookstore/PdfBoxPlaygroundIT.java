package hipravin.jarvis.bookstore;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PdfBoxPlaygroundIT {

    @Test
    @Disabled
    void sampleReadPdf() throws IOException {
        String samplePdfFile = "src/test/resources/data/bookstore/garlic-onion-15.JChromat.A2006.pdf";
//        String samplePdfFile = "src/test/resources/data/bookstore/estimating salt intake not so easy.pdf";
        assertTrue(Files.isRegularFile(Path.of(samplePdfFile)));

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(samplePdfFile))) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(0);
            stripper.setEndPage(0);
            String text = stripper.getText(document);


            System.out.println(text);


//            for (PDPage page : document.getPages()) {
//
//                String contents = new String(page.getContents().readAllBytes());
//
//                System.out.println("===== PAGE " + page.getAnnotations() +"  ===========");
//
//                System.out.println(contents);
//
//                System.out.println("=======================");
//            }
        }

    }

    @Test
    @Disabled
    void sampleSplitPagesPdf() throws IOException {
        String samplePdfFile = "src/test/resources/data/bookstore/garlic-onion-15.JChromat.A2006.pdf";
        assertTrue(Files.isRegularFile(Path.of(samplePdfFile)));

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(samplePdfFile))) {

//            for (int i = 0; i < 19; i++) {
//
//
//                document.removePage(1);
//            }

            int pageNum = 2;

            for (int i = 0; i < (pageNum - 1); i++) {
                 document.removePage(0);
            }

            while(document.getNumberOfPages() > 1) {
                document.removePage(document.getNumberOfPages() - 1);
            }

            document.save(Path.of("generated-cropped.pdf").toFile());
        }
    }
}
