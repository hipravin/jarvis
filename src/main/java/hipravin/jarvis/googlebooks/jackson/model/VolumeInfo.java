package hipravin.jarvis.googlebooks.jackson.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VolumeInfo(
        String title,
        List<String> authors,
        String publisher,
        @JsonProperty("publishedDate")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd") LocalDate publishedDate,
        String description,
        String language,
        String previewLink,
        String infoLink,
        String canonicalVolumeLink
) {
}
