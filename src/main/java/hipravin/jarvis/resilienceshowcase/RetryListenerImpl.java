package hipravin.jarvis.resilienceshowcase;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.retry.*;
import org.springframework.stereotype.Component;

//Can be used only with RetryTemplate
//Not suitable for @Retryable
@Component
public class RetryListenerImpl implements RetryListener {
    private static final Logger log = LoggerFactory.getLogger(RetryListenerImpl.class);

    @Override
    public void beforeRetry(RetryPolicy retryPolicy, Retryable<?> retryable) {
        log.info("Before retry");
    }

    @Override
    public void onRetrySuccess(RetryPolicy retryPolicy, Retryable<?> retryable, @Nullable Object result) {
        log.info("On retry success");
    }

    @Override
    public void onRetryFailure(RetryPolicy retryPolicy, Retryable<?> retryable, Throwable throwable) {
        log.info("On retry failure");
    }

    @Override
    public void onRetryableExecution(RetryPolicy retryPolicy, Retryable<?> retryable, RetryState retryState) {
        log.info("On retryable execution");
    }

    @Override
    public void onRetryPolicyExhaustion(RetryPolicy retryPolicy, Retryable<?> retryable, RetryException exception) {
        log.info("On retry policy exhaustion");
    }

    @Override
    public void onRetryPolicyInterruption(RetryPolicy retryPolicy, Retryable<?> retryable, RetryException exception) {
        log.info("On retry policy interruption");
    }

    @Override
    public void onRetryPolicyTimeout(RetryPolicy retryPolicy, Retryable<?> retryable, RetryException exception) {
        log.info("On retry policy timeout");
    }
}
