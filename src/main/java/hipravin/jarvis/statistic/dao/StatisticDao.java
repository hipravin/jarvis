package hipravin.jarvis.statistic.dao;

import hipravin.jarvis.statistic.dto.QueryGithubStatDto;

import java.util.Map;
import java.util.SequencedMap;

public interface StatisticDao {
    /**
     *
     * @param stat
     * @return id of new <code>QueryEntity</code> inserted
     */
    long save(QueryGithubStatDto stat);

    /**
     * Returns github users that have more occurences in search results than others.
     *
     * @param limit
     * @return Map of user id to occurence count ordered by value desc, user id asc
     */
    SequencedMap<String, Long> topGithubUsers(int limit);
}
