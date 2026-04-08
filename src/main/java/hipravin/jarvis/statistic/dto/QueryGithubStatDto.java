package hipravin.jarvis.statistic.dto;

import java.time.Duration;
import java.util.List;

public record QueryGithubStatDto (
        String query,
        Duration executionTime,
        List<UserStat> githubUserResults
){
}
