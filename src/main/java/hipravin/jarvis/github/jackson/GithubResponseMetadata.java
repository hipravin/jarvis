package hipravin.jarvis.github.jackson;

import java.net.http.HttpResponse;

public record GithubResponseMetadata(

) {
    public static <T> GithubResponseMetadata fromHttpResponse(HttpResponse<T> httpResponse) {
        return null; //TODO: implement?
    }
}
