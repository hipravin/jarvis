package hipravin.jarvis.googlebooks.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BooksVolumes(
        String kind,
        Long totalItems,
        List<BooksVolume> items
) {
}
