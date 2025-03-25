package hipravin.jarvis.bookstore;

import hipravin.jarvis.bookstore.load.BookLoader;
import hipravin.jarvis.bookstore.load.BookstoreProperties;
import hipravin.jarvis.bookstore.load.DirectoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {BookstoreProperties.class})
public class BookstoreConfig {
    private static final Logger log = LoggerFactory.getLogger(BookstoreConfig.class);

    @Autowired
    private BookstoreProperties bookstoreProperties;

    @Bean
    public ApplicationRunner preloadPdfFiles() {
        return args -> {
            var pdfs = DirectoryUtil.findFilesRecursively(bookstoreProperties.loaderRootPath(), "pdf");

            log.info("Going to load from files: ");
            pdfs.forEach(p -> log.info(p.toString()));
        };
    }

}
