package hipravin.jarvis.engine.model;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Objects;

public enum InformationSource {
    GITHUB("GH"),
    GOOGLE_BOOKS("GB"),
    BOOKSTORE("BS"),
    STACKEXCHANGE("SE");

    private final String alias;

    InformationSource(String alias) {
        this.alias = alias;
    }

    public String alias() {
        return alias;
    }

    public static InformationSource fromAlias(String alias) {
        if(!StringUtils.hasText(alias)) {
            return null;
        }

        return Arrays.stream(values())
                .filter(se -> se.alias.equalsIgnoreCase(alias) || se.name().equalsIgnoreCase(alias))
                .findAny().orElseThrow(() -> new IllegalArgumentException("Invalid information source alias: " + alias));
    }
}
