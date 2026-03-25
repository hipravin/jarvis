package hipravin.jarvis.enginev2.dto;

import hipravin.jarvis.engine.model.InformationSource;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public record Excerpt(
        InformationSource source,
        Link title,
        TextBlock main
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private InformationSource source;
        private Link title;
        private TextBlock main;

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

        public Excerpt build() {
            return new Excerpt(
                    Objects.requireNonNull(source),
                    Objects.requireNonNull(title),
                    Objects.requireNonNull(main));
        }
    }
}
