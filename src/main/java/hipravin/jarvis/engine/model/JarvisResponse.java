package hipravin.jarvis.engine.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
public record JarvisResponse(
        @JsonProperty("response") String response,
        @JsonProperty("author_results") List<AuthorResult> authorResults,
        @JsonProperty("code_fragments") List<CodeFragment> codeFragments
) {
}
