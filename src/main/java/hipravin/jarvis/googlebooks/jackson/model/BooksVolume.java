package hipravin.jarvis.googlebooks.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.util.StringUtils;

import java.util.function.Predicate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BooksVolume(
        String kind,
        VolumeInfo volumeInfo,
        SearchInfo searchInfo

) {
    public static Predicate<BooksVolume> HAS_TEXT_SNIPPET = (bv ->
            bv.searchInfo() != null
                    && StringUtils.hasText(bv.searchInfo.textSnippet()));

    public static Predicate<BooksVolume> HAS_PREVIEW_LINK = (bv ->
            bv.volumeInfo() != null
                    && StringUtils.hasText(bv.volumeInfo().previewLink()));

}
