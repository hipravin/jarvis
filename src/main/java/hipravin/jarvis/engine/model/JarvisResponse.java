package hipravin.jarvis.engine.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public record JarvisResponse(
        @JsonProperty("response") String response,
        @JsonProperty("authors") List<AuthorResult> authorResults,
        @JsonProperty("code_fragments") List<CodeFragment> codeFragments
) {
    public static JarvisResponse EMPTY_RESPONSE = new JarvisResponse("", Collections.emptyList(), Collections.emptyList());
}
