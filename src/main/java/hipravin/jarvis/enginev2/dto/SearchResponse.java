package hipravin.jarvis.enginev2.dto;

import hipravin.jarvis.engine.model.InformationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public record SearchResponse(
        List<Excerpt> excerpts,
        List<Error> errors
) {

    public static SearchResponse success(List<Excerpt> excerpts) {
        return new SearchResponse(excerpts, Collections.emptyList());
    }

    public static SearchResponse failed(Throwable t) {
        return new SearchResponse(Collections.emptyList(), List.of(new Error(t.getMessage())));
    }

    public static SearchResponse failed(String message) {
        return new SearchResponse(Collections.emptyList(), List.of(new Error(message)));
    }

    public static SearchResponse join(SearchResponse... responses) {
        List<Error> errors = Arrays.stream(responses)
                .flatMap(sr -> sr.errors().stream())
                .toList();

        List<Excerpt> excerpts = Arrays.stream(responses)
                .flatMap(sr -> sr.excerpts().stream())
                .toList();

        return new SearchResponse(excerpts, errors);
    }
}
