package hipravin.jarvis.statistic.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record QueryGithubUserId(
        @Column(name = "QUERY_ID") Long queryId,
        @Column(name = "GITHUB_USER_ID") String githubUserId
) {
}
