package hipravin.jarvis.enginev2.dto;

import hipravin.jarvis.engine.model.InformationSource;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public record Excerpt(
        InformationSource source,
        Map<String, Object> metadata,
        Link title,
        TextBlock main
) {
    public static String METADATA_GH_USERS = "github-users";//Map[hipravin:3, josepaumard:7, ...]

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private InformationSource source;
        private Link title;
        private TextBlock main;
        private Map<String, Object> metadata = Collections.emptyMap();

        Builder() {
        }

        public Builder source(InformationSource source) {
            this.source = Objects.requireNonNull(source);
            return this;
        }

        public Builder title(String title, @Nullable String url) {
            this.title = new Link(Objects.requireNonNull(title), url);
            return this;
        }

        public Builder mainHtml(String html) {
            this.main = new TextBlock(Objects.requireNonNull(html), TextFormat.HTML);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = Objects.requireNonNull(metadata);
            return this;
        }

        public Excerpt build() {
            return new Excerpt(
                    Objects.requireNonNull(source),
                    Objects.requireNonNull(metadata),
                    Objects.requireNonNull(title),
                    Objects.requireNonNull(main));
        }
    }
}
