package hipravin.jarvis;

import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.AggregatingSearchService;
import hipravin.jarvis.enginev2.dto.SearchRequest;
import hipravin.jarvis.enginev2.dto.SearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.EnumSet;
import java.util.List;

import static hipravin.jarvis.engine.model.InformationSource.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = {SearchController.class})
@ActiveProfiles("test")
class SearchControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AggregatingSearchService searchService;

    @BeforeEach
    void setUp() {
        Mockito.reset(searchService);
    }

    @Test
    @WithMockUser(username = "anonymous")
    void searchSampleQuery() throws Exception {
        when(searchService.search(any())).thenReturn(SearchResponse.success(List.of()));

        mockMvc.perform(get("/api/search")
                        .param("q", "query1")
                        .param("is", "GH", "SE", "BS"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(searchService, times(1)).search(
                eq(new SearchRequest("query1", EnumSet.of(GITHUB, STACKEXCHANGE, BOOKSTORE))));
    }

    @Test
    @WithMockUser(username = "anonymous")
    void searchSampleQueryIsUnspecified() throws Exception {
        when(searchService.search(any())).thenReturn(SearchResponse.success(List.of()));

        mockMvc.perform(get("/api/search")
                        .param("q", "query1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(searchService, times(1)).search(
                eq(new SearchRequest("query1", EnumSet.allOf(InformationSource.class))));
    }

    @Test
    @WithMockUser(username = "anonymous")
    void searchInvalidAlias() throws Exception {
        when(searchService.search(any())).thenReturn(SearchResponse.success(List.of()));

        MvcResult mvcResult = mockMvc.perform(get("/api/search")
                        .param("q", "query1")
                        .param("is", "ZZ", "SE", "BS"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        var problem = new JsonMapper().readValue(json, ProblemDetail.class);

        verify(searchService, never()).search(any());

        assertNotNull(problem.getDetail());
        assertTrue(problem.getDetail().contains("Invalid"));
        assertTrue(problem.getDetail().contains("ZZ"));
    }

    @Test
    @WithMockUser(username = "anonymous")
    void searchBlank() throws Exception {
        when(searchService.search(any())).thenReturn(SearchResponse.success(List.of()));

        mockMvc.perform(get("/api/search")
                        .param("q", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(searchService, never()).search(any());
    }
}