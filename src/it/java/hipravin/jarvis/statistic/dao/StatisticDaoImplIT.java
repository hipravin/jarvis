package hipravin.jarvis.statistic.dao;

import hipravin.jarvis.JarvisIntegrationTest;
import hipravin.jarvis.statistic.dto.QueryGithubStatDto;
import hipravin.jarvis.statistic.dto.UserStat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

@JarvisIntegrationTest
class StatisticDaoImplIT {
    @Autowired
    StatisticDao statisticDao;

    @Test
    void saveAndQuery() {
        long added = statisticDao.save(new QueryGithubStatDto("query1",
                Duration.ofMillis(100), List.of(
                new UserStat("john", 1),
                new UserStat("jane", 7),
                new UserStat("pravin", 117)
        )));
        assertTrue(added > 0);

        long addedRepeatingUsers = statisticDao.save(new QueryGithubStatDto("query2",
                Duration.ofMillis(100), List.of(
                new UserStat("john", 3),
                new UserStat("pravin", 8),
                new UserStat("kolya", 19),
                new UserStat("tolya", 1111)
        )));
        assertTrue(addedRepeatingUsers > 0);

        Map<String, Long> top = statisticDao.topGithubUsers(3);

        SequencedMap<String, Long> expected = new LinkedHashMap<>();
        expected.put("tolya", 1111L);
        expected.put("pravin", 117 + 8L);
        expected.put("kolya", 19L);

        assertEquals(expected, top);
    }
}