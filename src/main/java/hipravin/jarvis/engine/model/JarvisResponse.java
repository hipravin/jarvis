package hipravin.jarvis.engine.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
public record JarvisResponse(
        @JsonProperty("response") String response,
        @JsonProperty("code") String code,
        @JsonProperty("code_fragments") List<CodeFragment> codeFragments
) {
}
