package hipravin.jarvis.github.jackson.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;


public class TextMatchDeserializer extends StdDeserializer<TextMatch> {

    public TextMatchDeserializer() {
        this(null);
    }

    public TextMatchDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public TextMatch deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String text = node.get("text").asText();

        int start;
        int end;

        if (node.get("indices") instanceof ArrayNode indices
                && indices.size() == 2) {

            start = indices.get(0).asInt();
            end = indices.get(1).asInt();
        } else {
            throw new JsonMappingException("Unexpected indices: " + node.get("indices").asText());
        }

        return new TextMatch(start, end, text);
    }
}