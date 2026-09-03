package co.com.nequi.franchise.mongo.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;


@Configuration
public class ResilienceConfig {

    public static final String MONGO_INSTANCE = "mongoFranchise";

    @Bean
    public CircuitBreaker franchiseCircuitBreaker(
            @Value("${resilience.mongo.circuit-breaker.failure-rate-threshold:50}") float failureRateThreshold,
            @Value("${resilience.mongo.circuit-breaker.sliding-window-size:10}") int slidingWindowSize,
            @Value("${resilience.mongo.circuit-breaker.wait-duration-seconds:5}") long waitDurationSeconds) {

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .slidingWindowSize(slidingWindowSize)
                .waitDurationInOpenState(Duration.ofSeconds(waitDurationSeconds))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();

        return CircuitBreaker.of(MONGO_INSTANCE, config);
    }

    @Bean
    public TimeLimiter franchiseTimeLimiter(
            @Value("${resilience.mongo.time-limiter.timeout-seconds:3}") long timeoutSeconds) {

        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(timeoutSeconds))
                .build();

        return TimeLimiter.of(MONGO_INSTANCE, config);
    }

    @Bean
    public Retry franchiseRetry(
            @Value("${resilience.mongo.retry.max-attempts:3}") int maxAttempts,
            @Value("${resilience.mongo.retry.wait-duration-millis:200}") long waitDurationMillis) {

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(Duration.ofMillis(waitDurationMillis))
                .failAfterMaxAttempts(true)
                .build();

        return Retry.of(MONGO_INSTANCE, config);
    }
}
