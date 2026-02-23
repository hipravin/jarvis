package hipravin.jarvis.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Component
public class JarvisIfoContributor implements InfoContributor {
    private static final Logger log = LoggerFactory.getLogger(JarvisIfoContributor.class);

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("custom", Collections.singletonMap("now",
                OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
    }

}