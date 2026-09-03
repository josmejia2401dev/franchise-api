package co.com.nequi.franchise.api.validation;

import co.com.nequi.franchise.api.dto.FieldViolation;
import co.com.nequi.franchise.api.error.InvalidRequestException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final Validator validator;

    public <T> Mono<T> validate(T target) {
        return Mono.fromCallable(() -> validator.validate(target))
                .flatMap(violations -> toFieldViolations(violations)
                        .filter(List::isEmpty)
                        .map(empty -> target)
                        .switchIfEmpty(rejected(violations, target)));
    }

    private <T> Mono<T> rejected(Set<ConstraintViolation<T>> violations, T target) {
        return toFieldViolations(violations)
                .flatMap(fieldViolations -> Mono.error(
                        new InvalidRequestException("Request validation failed", fieldViolations)));
    }

    private <T> Mono<List<FieldViolation>> toFieldViolations(Set<ConstraintViolation<T>> violations) {
        return Flux.fromIterable(violations)
                .map(violation -> new FieldViolation(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .collectList();
    }
}
