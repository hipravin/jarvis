package hipravin.jarvis.enginev2.dto;

import hipravin.jarvis.engine.model.InformationSource;

public record Excerpt(
        InformationSource source,
        HeaderBlock header,
        TextBlock main
) {
}
