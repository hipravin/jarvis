package hipravin.jarvis.bookstore.load;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "bookstore")
@Validated
public record BookstoreProperties(
        @NotNull Path loaderRootPath
) {

}
