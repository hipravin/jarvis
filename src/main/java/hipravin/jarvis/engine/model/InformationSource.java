package hipravin.jarvis.engine.model;

import org.springframework.util.StringUtils;

import java.util.Arrays;

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

    public static InformationSource fromString(String searchEngineType) {
        if(!StringUtils.hasText(searchEngineType)) {
            return null;
        }

        return Arrays.stream(values())
                .filter(se -> se.alias.equalsIgnoreCase(searchEngineType) || se.name().equalsIgnoreCase(searchEngineType))
                .findAny().orElseThrow(() -> new IllegalArgumentException("Invalid search provider: " + searchEngineType));
    }
}
