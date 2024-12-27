package hipravin.jarvis.engine.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public record JarvisResponse(
        @JsonProperty("response") String response,
        @JsonProperty("items") List<ResponseItem> responseItems,
        @JsonProperty("code_fragments") List<CodeFragment> codeFragments
) {
    public static JarvisResponse EMPTY_RESPONSE = new JarvisResponse("", Collections.emptyList(),
            Collections.emptyList());

    public static JarvisResponse ofMessage(String message) {
        return EMPTY_RESPONSE.orElse(message);
    }

    public static JarvisResponse combine(JarvisResponse... partialResponses) {

        String responseCombined = Arrays.stream(partialResponses)
                .map(JarvisResponse::response)
                .collect(Collectors.joining("\n"));

        List<ResponseItem> itemsCombined = combineSubCollection(JarvisResponse::responseItems, partialResponses);
        List<CodeFragment> codeFragmentsCombined = combineSubCollection(JarvisResponse::codeFragments, partialResponses);

        return new JarvisResponse(responseCombined, itemsCombined, codeFragmentsCombined);
    }

    private static <T> List<T> combineSubCollection(Function<JarvisResponse, List<T>> getFunction,
                                                    JarvisResponse... responses) {
        return Arrays.stream(responses).flatMap(r -> getFunction.apply(r).stream())
                .toList();
    }

    public JarvisResponse orElse(String emptyResponseMessage) {
        if(response().isBlank()
                && responseItems().isEmpty()
                && codeFragments().isEmpty()) {
            return new JarvisResponse(emptyResponseMessage, Collections.emptyList(), Collections.emptyList());
        } else {
            return this;
        }
    }
}
