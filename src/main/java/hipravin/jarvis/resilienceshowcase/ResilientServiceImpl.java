package hipravin.jarvis.resilienceshowcase;

import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ResilientServiceImpl implements ResilientService {

    final AtomicLong callCounter = new AtomicLong(0);
    final AtomicLong concurrentCallCounter = new AtomicLong(0);
    final AtomicLong exceptionCounter = new AtomicLong(0);

    @Retryable(
            includes = {RuntimeException.class},
            maxRetries = 10,
            delay = 50,
            jitter = 10, //delay +- jitter
            multiplier = 2,
            maxDelay = 200)
    @ConcurrencyLimit(5)
    @Override
    public ResilientResultDto callExternalService() {
        concurrentCallCounter.incrementAndGet();
        long delayMs = ThreadLocalRandom.current().nextInt(30);
        boolean throwRetriable = ThreadLocalRandom.current().nextInt(5) == 0;

        try {
            try {
                Thread.sleep(Duration.ofMillis(delayMs));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (throwRetriable) {
                exceptionCounter.incrementAndGet();
                throw new RuntimeException("Random exception");
            }

            return new ResilientResultDto(
                    callCounter.getAndIncrement(),
                    concurrentCallCounter.get(),
                    String.valueOf(Thread.currentThread()));
        } finally {
            concurrentCallCounter.decrementAndGet();
        }
    }

    public void resetCounters() {
        exceptionCounter.set(0);
        callCounter.set(0);
        concurrentCallCounter.set(0);
    }

    public AtomicLong getExceptionCounter() {
        return exceptionCounter;
    }
}
