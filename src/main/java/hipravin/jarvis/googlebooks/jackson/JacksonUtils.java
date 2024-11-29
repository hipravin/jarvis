package hipravin.jarvis.googlebooks.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hipravin.jarvis.github.jackson.model.TextMatch;
import hipravin.jarvis.github.jackson.model.TextMatchDeserializer;

public abstract class JacksonUtils {
    private JacksonUtils() {
    }

    public static ObjectMapper createGoogleBooksObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        return mapper;
    }
}
