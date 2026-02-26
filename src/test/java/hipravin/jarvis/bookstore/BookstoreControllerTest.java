package hipravin.jarvis.bookstore;

import hipravin.jarvis.bookstore.dao.BookstoreDao;
import hipravin.jarvis.bookstore.load.BookReader;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {BookstoreController.class})
class BookstoreControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    BookstoreDao bookstoreDao;
    @MockitoBean
    BookReader bookReader;

    @Test
    void rawpdfFound() throws Exception {
        byte[] expectedByteContent = "expected pdf content".getBytes(StandardCharsets.UTF_8);

        var osCapture = ArgumentCaptor.forClass(OutputStream.class);

        doAnswer(a -> {
            osCapture.getValue().write(expectedByteContent);
            osCapture.getValue().close();
            return null;
        }).when(bookstoreDao).writePdfContentTo(eq(1L), osCapture.capture());

        mockMvc.perform(get("/api/v1/bookstore/book/{id}/rawpdf", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(expectedByteContent));
    }
}