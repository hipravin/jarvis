package hipravin.jarvis.bookstore;

import hipravin.jarvis.bookstore.dao.BookstoreDao;
import hipravin.jarvis.bookstore.load.BookReader;
import hipravin.jarvis.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    void testRawpdfFound() throws Exception {
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

    @Test
    void testDelete() throws Exception {
        doNothing().when(bookstoreDao).deleteById(eq(1L));
        mockMvc.perform(delete("/api/v1/bookstore/book/{id}", 1))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(bookstoreDao, times(1)).deleteById(eq(1L));
    }

    @Test
    void testDeleteNotFound() throws Exception {
        JsonMapper mapper = new JsonMapper();
        doThrow(new NotFoundException("Book 1404")).when(bookstoreDao).deleteById(eq(1404L));
        var mvcResult = mockMvc.perform(delete("/api/v1/bookstore/book/{id}", 1404))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        var errorResponse = mapper.readValue(mvcResult.getResponse().getContentAsString(), ProblemDetail.class);
        assertEquals("Not Found", errorResponse.getTitle());
        assertTrue(errorResponse.getDetail().contains("1404"));
    }
}