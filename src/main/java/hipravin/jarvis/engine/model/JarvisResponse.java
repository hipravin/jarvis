package hipravin.jarvis.engine.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record JarvisResponse(
        @JsonProperty("response") String response,
        @JsonProperty("authors") List<AuthorResult> authorResults,
        @JsonProperty("code_fragments") List<CodeFragment> codeFragments
) {
    public static JarvisResponse EMPTY_RESPONSE = new JarvisResponse("", Collections.emptyList(), Collections.emptyList());

    public static JarvisResponse combine(JarvisResponse... partialResponses) {
        String responseCombined = Arrays.stream(partialResponses)
                .map(JarvisResponse::response)
                .collect(Collectors.joining("\n"));


        List<AuthorResult> authorsCombined = Arrays.stream(partialResponses)
                .flatMap(r -> r.authorResults().stream())
                .toList();

        List<CodeFragment> codeFragmentsCombined = Arrays.stream(partialResponses)
                .flatMap(r -> r.codeFragments().stream())
                .toList();

        return new JarvisResponse(responseCombined, authorsCombined, codeFragmentsCombined);
    }
}
