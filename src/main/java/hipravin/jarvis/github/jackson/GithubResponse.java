package hipravin.jarvis.github.jackson;

public record GithubResponse<T>(
        GithubResponseMetadata metadata,
        T body
) {
}
