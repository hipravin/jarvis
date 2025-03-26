package hipravin.jarvis.bookstore;

import hipravin.jarvis.bookstore.load.BookstoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {BookstoreProperties.class})
public class BookstoreConfig {
    private static final Logger log = LoggerFactory.getLogger(BookstoreConfig.class);

    private final BookstoreProperties bookstoreProperties;
    private final BookstoreLoadService bookstoreLoadService;

    public BookstoreConfig(BookstoreProperties bookstoreProperties, BookstoreLoadService bookstoreLoadService) {
        this.bookstoreProperties = bookstoreProperties;
        this.bookstoreLoadService = bookstoreLoadService;
    }

    @Bean
    @ConditionalOnProperty(
            value = "bookstore.initialload.enabled",
            havingValue = "true"
    )
    public ApplicationRunner initialLoadPdfFiles() {
        return args -> {
            log.info("Running initial pdf load: {}", bookstoreProperties);
            bookstoreLoadService.loadAll();
            log.info("Finished initial pdf load.");
        };
    }

}
