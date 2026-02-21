package hipravin.jarvis.statistic;

import hipravin.jarvis.engine.model.JarvisRequest;
import hipravin.jarvis.engine.model.JarvisResponse;
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

    static class Pointcats {
        @Pointcut("execution(* hipravin.jarvis.engine.SearchEngine.search(..))")
        public void search() {
        }
    }

    @Around("hipravin.jarvis.statistic.SearchStatAspect.Pointcats.search()")
    public Object doConcurrentOperation(ProceedingJoinPoint pjp) throws Throwable {

        long start = System.nanoTime();

        var retval = pjp.proceed();
        if (pjp.getArgs().length > 0 && pjp.getArgs()[0] instanceof JarvisRequest request
                && retval instanceof JarvisResponse response) {
            eventPublisher.publishEvent(SearchCompletedEvent.of(request, response, Duration.ofNanos(System.nanoTime() - start),
                    this));
        }

        return retval;
    }
}
