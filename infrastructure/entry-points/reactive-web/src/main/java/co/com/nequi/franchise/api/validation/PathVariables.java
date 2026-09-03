package co.com.nequi.franchise.api.validation;

import co.com.nequi.franchise.api.error.InvalidRequestException;
import lombok.experimental.UtilityClass;
import reactor.core.publisher.Mono;

@UtilityClass
public class PathVariables {

    public Mono<String> requireNonBlank(String value, String field) {
        return Mono.justOrEmpty(value)
                .map(String::trim)
                .filter(trimmed -> !trimmed.isEmpty())
                .switchIfEmpty(Mono.error(
                        InvalidRequestException.ofField(field, field + " must not be blank")));
    }
}
