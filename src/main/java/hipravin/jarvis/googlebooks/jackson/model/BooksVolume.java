package hipravin.jarvis.googlebooks.jackson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Optional;
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

    public Optional<String> yearPublished() {
        return Optional.ofNullable(volumeInfo())
                .map(VolumeInfo::publishedDate)
                .flatMap(BooksVolume::tryParseDateYear)
                .map(String::valueOf);
    }

    static Optional<Integer> tryParseDateYear(String dateString) {
        try {
            var localDate = DateTimeFormatter.ISO_LOCAL_DATE.parse(dateString, LocalDate::from);
            return Optional.of(localDate.getYear());
        } catch (RuntimeException e) {
            return tryParseYear(dateString);
        }
    }

    static Optional<Integer> tryParseYear(String dateString) {
        try {
            var justYear = DateTimeFormatter.ofPattern("yyyy").parse(dateString);
            return Optional.of(justYear.get(ChronoField.YEAR));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

}
