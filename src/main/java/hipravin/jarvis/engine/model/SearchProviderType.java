package hipravin.jarvis.engine.model;

import org.springframework.util.StringUtils;

import java.util.Arrays;

public enum SearchProviderType {
    GITHUB("GH"),
    GOOGLE_BOOKS("GB"),
    BOOKSTORE("BS");

    private final String alias;

    SearchProviderType(String alias) {
        this.alias = alias;
    }

    public String alias() {
        return alias;
    }

    public static SearchProviderType fromString(String searchEngineType) {
        if(!StringUtils.hasText(searchEngineType)) {
            return null;
        }

        return Arrays.stream(values())
                .filter(se -> se.alias.equalsIgnoreCase(searchEngineType) || se.name().equalsIgnoreCase(searchEngineType))
                .findAny().orElseThrow(() -> new IllegalArgumentException("Invalid search provider: " + searchEngineType));
    }
}
