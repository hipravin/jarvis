package hipravin.jarvis.config;

import hipravin.jarvis.bookstore.BookstoreConfig;
import hipravin.jarvis.github.GithubConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAspectJAutoProxy
@Import({GithubConfig.class, BookstoreConfig.class})
public class JarvisConfig {


}
