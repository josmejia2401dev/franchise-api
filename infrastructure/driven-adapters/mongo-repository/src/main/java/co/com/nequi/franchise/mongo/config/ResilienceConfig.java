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
            @Value("${resilience.mongo.circuit-breaker.sliding-window-type:COUNT_BASED}") String slidingWindowType,
            @Value("${resilience.mongo.circuit-breaker.sliding-window-size:10}") int slidingWindowSize,
            @Value("${resilience.mongo.circuit-breaker.minimum-number-of-calls:5}") int minimumNumberOfCalls,
            @Value("${resilience.mongo.circuit-breaker.failure-rate-threshold:50}") float failureRateThreshold,
            @Value("${resilience.mongo.circuit-breaker.slow-call-rate-threshold:100}") float slowCallRateThreshold,
            @Value("${resilience.mongo.circuit-breaker.slow-call-duration-threshold-seconds:2}") long slowCallDurationSeconds,
            @Value("${resilience.mongo.circuit-breaker.wait-duration-in-open-state-seconds:5}") long waitDurationSeconds,
            @Value("${resilience.mongo.circuit-breaker.permitted-calls-in-half-open-state:3}") int permittedCallsInHalfOpen,
            @Value("${resilience.mongo.circuit-breaker.automatic-transition-enabled:true}") boolean automaticTransition) {

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.valueOf(slidingWindowType))
                .slidingWindowSize(slidingWindowSize)
                .minimumNumberOfCalls(minimumNumberOfCalls)
                .failureRateThreshold(failureRateThreshold)
                .slowCallRateThreshold(slowCallRateThreshold)
                .slowCallDurationThreshold(Duration.ofSeconds(slowCallDurationSeconds))
                .waitDurationInOpenState(Duration.ofSeconds(waitDurationSeconds))
                .permittedNumberOfCallsInHalfOpenState(permittedCallsInHalfOpen)
                .automaticTransitionFromOpenToHalfOpenEnabled(automaticTransition)
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
