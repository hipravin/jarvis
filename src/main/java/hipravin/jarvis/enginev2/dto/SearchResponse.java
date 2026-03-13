package hipravin.jarvis.enginev2.dto;

import java.util.List;

public record SearchResponse(
        List<Item> items,
        List<Error> errors
) {
}
