package hipravin.jarvis.statistic;

import hipravin.jarvis.enginev2.dto.SearchRequest;
import hipravin.jarvis.enginev2.dto.SearchResponse;
import hipravin.jarvis.event.SearchCompletedEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Aspect
@Component
public class SearchStatAspect {
    private final ApplicationEventPublisher eventPublisher;

    public SearchStatAspect(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    static class Pointcuts {
        @Pointcut("execution(* hipravin.jarvis.enginev2.AggregatingSearchService.search(..))")
        public void search() {
        }
    }

    @Around("hipravin.jarvis.statistic.SearchStatAspect.Pointcuts.search()")
    public Object searchStat(ProceedingJoinPoint pjp) throws Throwable {

        long start = System.nanoTime();

        var retval = pjp.proceed();
        if (pjp.getArgs().length > 0 && pjp.getArgs()[0] instanceof SearchRequest request
                && retval instanceof SearchResponse response) {
            eventPublisher.publishEvent(new SearchCompletedEvent(this,
                    request, response, Duration.ofNanos(System.nanoTime() - start)));
        }

        return retval;
    }
}
