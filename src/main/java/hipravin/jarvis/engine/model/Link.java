package hipravin.jarvis.engine.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Link(
        String title,
        String href
) {
    public static Link fromGithubHtmlUrl(String htmlUrl) {
        //https://github.com/hipravin/devcompanion/blob/2a9f8aafb729a0975e80eb34c9e94a6c7a00c421/development/playground/playground-sql/postgresql-book/queries.sql
        Pattern pattern = Pattern.compile("^https://github.com/([^/]+)/([^/]+)/.+/([^/]+)$");
        Matcher matcher = pattern.matcher(htmlUrl);
        if (!matcher.find() || matcher.groupCount() < 3) {
            return new Link(htmlUrl, htmlUrl);
        }

        String title = matcher.group(1) + "-"
                + matcher.group(2) + "/.../"
                + matcher.group(3);

        return new Link(title, htmlUrl);
    }
}
