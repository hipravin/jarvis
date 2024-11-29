package hipravin.jarvis.googlebooks.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BooksVolume(
        String kind,
        VolumeInfo volumeInfo,
        SearchInfo searchInfo

) {
}
