package hipravin.jarvis.statistic;

import hipravin.jarvis.enginev2.dto.Excerpt;
import hipravin.jarvis.statistic.dao.StatisticDao;
import hipravin.jarvis.statistic.dto.QueryGithubStatDto;
import hipravin.jarvis.statistic.dto.SearchCompletedDto;
import hipravin.jarvis.statistic.dto.UserStat;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticProcessorServiceImpl {
    private final StatisticDao statisticDao;

    public StatisticProcessorServiceImpl(StatisticDao statisticDao) {
        this.statisticDao = statisticDao;
    }

    public void process(SearchCompletedDto searchCompletedDto) {
        QueryGithubStatDto stat = map(searchCompletedDto);

        statisticDao.save(stat);
    }

    private QueryGithubStatDto map(SearchCompletedDto dto) {
        Map<String, Long> userOccurenceMap = new LinkedHashMap<>();
        for (Excerpt excerpt : dto.response().excerpts()) {
            if (excerpt.metadata() != null
                    && excerpt.metadata().get(Excerpt.METADATA_GH_USERS) instanceof Map<?, ?> ghUsers) {
                ghUsers.forEach((k, v) -> {
                    if((k instanceof String user) && (v instanceof Number count)) {
                        userOccurenceMap.merge(user, count.longValue(), (a,b) -> a);//use first value and reject subsequent
                    }
                });
            }
        }

        List<UserStat> userStats = userOccurenceMap.entrySet().stream()
                .map(e -> new UserStat(e.getKey(), e.getValue()))
                .toList();

        return new QueryGithubStatDto(dto.request().query(), dto.elapsed(), userStats);
    }
}
