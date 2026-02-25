package hipravin.jarvis.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Configuration
public class VersionInfoMeterRegistry {
    public static final String UNSPECIFIED = "-";

    public VersionInfoMeterRegistry(BuildProperties buildProperties,
                                    GitProperties gitProperties,
                                    MeterRegistry registry) {
        registerBuildInfoPropertiesGauge(buildProperties, gitProperties, registry);
    }

    private void registerBuildInfoPropertiesGauge(BuildProperties buildProperties,
                                                  GitProperties gitProperties,
                                                  MeterRegistry registry) {

        Gauge.builder("app_build_info", 1.0, n -> n)
                .strongReference(true)
                .description("Application build info stored in metric tags.")
                .tags("app_version", orElseUnspecified(buildProperties.getVersion()),
                        "app_name", orElseUnspecified(buildProperties.getName()),
                        "build_time", orElseUnspecified(buildProperties.getTime()),
                        "ci_build_number", orElseUnspecified(buildProperties.get("ci.number")),
                        "commit_id", orElseUnspecified(gitProperties.getCommitId()),
                        "short_commit_id", orElseUnspecified(gitProperties.getShortCommitId()))
                .register(registry);
    }

    private static String orElseUnspecified(String property) {
        return (property != null) ? property : UNSPECIFIED;
    }

    private static String orElseUnspecified(Instant property) {
        return (property != null)
                ? property.truncatedTo(ChronoUnit.SECONDS).toString()
                : UNSPECIFIED;
    }
}
