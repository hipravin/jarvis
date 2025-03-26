package hipravin.jarvis.bookstore.load.model;

import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Map;
import java.util.stream.Collectors;

public record BookMetadata(
        String title, //title from file metadata, can be empty or meaningless
        String author,
        String creator,
        String producer,
        OffsetDateTime creationDate,
        String keywords,
        Map<String, String> metadata
) {
    public static BookMetadata from(PDDocumentInformation documentInformation) {
        record KeyValue(String key, String value) {
        }

        Map<String, String> metadata = documentInformation.getMetadataKeys().stream()
                .map(key -> new KeyValue(key, documentInformation.getCustomMetadataValue(key)))
                .filter(kv -> StringUtils.hasText(kv.value()))
                .collect(Collectors.toMap(KeyValue::key, KeyValue::value));

        return new BookMetadata(
                documentInformation.getTitle(),
                documentInformation.getAuthor(),
                documentInformation.getCreator(),
                documentInformation.getProducer(),
                fromCalendar(documentInformation.getCreationDate()),
                documentInformation.getKeywords(),
                metadata
        );
    }

    static OffsetDateTime fromCalendar(Calendar calendar) {
        return calendar.toInstant().atZone(calendar.getTimeZone().toZoneId())
                .toOffsetDateTime();
    }
}
