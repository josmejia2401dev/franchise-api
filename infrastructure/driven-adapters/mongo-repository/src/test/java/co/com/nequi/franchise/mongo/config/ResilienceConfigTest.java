package co.com.nequi.franchise.mongo.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ResilienceConfigTest {

    private final ResilienceConfig config = new ResilienceConfig();

    @Test
    void buildsCircuitBreakerWithProvidedValues() {
        CircuitBreaker cb = config.franchiseCircuitBreaker(
                "COUNT_BASED", 10, 5, 50f, 100f, 2, 5, 3, true);

        assertThat(cb.getName()).isEqualTo(ResilienceConfig.MONGO_INSTANCE);
        assertThat(cb.getCircuitBreakerConfig().getSlidingWindowSize()).isEqualTo(10);
        assertThat(cb.getCircuitBreakerConfig().getMinimumNumberOfCalls()).isEqualTo(5);
    }

    @Test
    void buildsTimeLimiterWithProvidedTimeout() {
        TimeLimiter tl = config.franchiseTimeLimiter(3);

        assertThat(tl.getName()).isEqualTo(ResilienceConfig.MONGO_INSTANCE);
        assertThat(tl.getTimeLimiterConfig().getTimeoutDuration()).isEqualTo(Duration.ofSeconds(3));
    }

    @Test
    void buildsRetryWithProvidedAttempts() {
        Retry retry = config.franchiseRetry(3, 200);

        assertThat(retry.getName()).isEqualTo(ResilienceConfig.MONGO_INSTANCE);
        assertThat(retry.getRetryConfig().getMaxAttempts()).isEqualTo(3);
    }
}
