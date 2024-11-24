package hipravin.jarvis.github;

import hipravin.jarvis.github.jackson.model.CodeSearchItem;

import static java.util.Optional.ofNullable;

public abstract class GithubUtils {
    private GithubUtils() {
    }
    public static String safeGetLogin(CodeSearchItem codeSearchItem) {
        return ofNullable(codeSearchItem)
                .flatMap(it -> ofNullable(it.repository()))
                .flatMap(it -> ofNullable(it.owner()))
                .flatMap(it -> ofNullable(it.login()))
                .map(String::toLowerCase)
                .orElse("");
    }
}
