package hipravin.jarvis.statistic.dao.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "QUERY_GITHUB_USER")
public class QueryGithubUserEntity {
    @EmbeddedId
    private QueryGithubUserId id;

    private Long count;

    public QueryGithubUserId getId() {
        return id;
    }

    public void setId(QueryGithubUserId id) {
        this.id = id;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}


