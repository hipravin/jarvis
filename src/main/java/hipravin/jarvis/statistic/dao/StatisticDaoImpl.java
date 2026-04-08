package hipravin.jarvis.statistic.dao;

import hipravin.jarvis.statistic.dao.entity.QueryEntity;
import hipravin.jarvis.statistic.dao.entity.QueryGithubUserId;
import hipravin.jarvis.statistic.dto.QueryGithubStatDto;
import hipravin.jarvis.statistic.dto.UserStat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class StatisticDaoImpl implements StatisticDao {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = false, isolation = Isolation.REPEATABLE_READ)
    //RR most likely not required because, but I'd like to experiment with it
    public long save(QueryGithubStatDto stat) {
        QueryEntity qe = new QueryEntity();
        qe.setQuery(stat.query());

        em.persist(qe);
        //batching is not feasible with 'on conflict'
        for (UserStat ghUserStat : stat.githubUserResults()) {
            em.createQuery("""
                            insert into GithubUserEntity (id)
                            values (:id)
                            on conflict(id) do nothing
                            """)
                    .setParameter("id", ghUserStat.userId())
                    .executeUpdate();

            em.createQuery("""
                            insert into QueryGithubUserEntity (id, count)
                            values (:id, :count)
                            on conflict(id) do nothing
                            """)
                    .setParameter("id", new QueryGithubUserId(qe.getId(), ghUserStat.userId()))
                    .setParameter("count", ghUserStat.count())
                    .executeUpdate();
        }

        em.flush();

        return qe.getId();
    }

    @Override
    public SequencedMap<String, Long> topGithubUsers(int limit) {
        record UserOccurence (String id, Long occurence){};
        var q = em.createNativeQuery("""
                        select id, occurences from (
                        select u.id, sum(qgu.count) as occurences from github_user u
                            join query_github_user qgu on u.id = qgu.github_user_id
                            group by u.id) x order by occurences desc, id limit :limit;
                        """, UserOccurence.class)
                .setParameter("limit", limit);
        @SuppressWarnings("unchecked")
        List<UserOccurence> userOccurences = (List<UserOccurence>) q.getResultList();

        return userOccurences.stream()
                .collect(Collectors.toMap(UserOccurence::id,
                        UserOccurence::occurence,
                        Long::sum, LinkedHashMap::new));
    }
}