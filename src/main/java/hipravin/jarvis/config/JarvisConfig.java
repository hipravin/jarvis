package hipravin.jarvis.config;

import hipravin.jarvis.bookstore.BookstoreConfig;
import hipravin.jarvis.github.GithubConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({GithubConfig.class, BookstoreConfig.class})
public class JarvisConfig {


}
