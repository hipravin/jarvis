package hipravin.jarvis.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import hipravin.jarvis.github.jackson.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

@Component
public class GithubApiClientImpl implements GithubApiClient, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(GithubApiClientImpl.class);

    private final ObjectMapper objectMapper = JacksonUtils.createGithubObjectMapper();
    private HttpClient httpClient;


    @Override
    public void destroy() throws Exception {
//        httpClient.close(); //since Java 23
    }
}
