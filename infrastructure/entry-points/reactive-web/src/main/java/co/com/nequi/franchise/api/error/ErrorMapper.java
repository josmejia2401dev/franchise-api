package co.com.nequi.franchise.api.error;

import co.com.nequi.franchise.api.dto.ErrorResponse;
import co.com.nequi.franchise.model.exception.BusinessException;
import co.com.nequi.franchise.model.exception.NotFoundException;
import co.com.nequi.franchise.model.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class ErrorMapper {

    private static final String INVALID_REQUEST = "INVALID_REQUEST";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private static final Map<Class<? extends BusinessException>, HttpStatus> BUSINESS_STATUS = Map.of(
            NotFoundException.class, HttpStatus.NOT_FOUND,
            ValidationException.class, HttpStatus.BAD_REQUEST
    );

    public ErrorMapping map(Throwable throwable) {
        return firstMatch(throwable)
                .orElseGet(() -> unexpected(throwable));
    }

    private Optional<ErrorMapping> firstMatch(Throwable throwable) {
        return invalidRequest(throwable)
                .or(() -> malformedBody(throwable))
                .or(() -> business(throwable))
                .or(() -> responseStatus(throwable));
    }

    private Optional<ErrorMapping> invalidRequest(Throwable throwable) {
        return cast(throwable, InvalidRequestException.class)
                .map(ex -> new ErrorMapping(HttpStatus.BAD_REQUEST,
                        ErrorResponse.of(INVALID_REQUEST, ex.getMessage(), ex.getViolations())));
    }

    private Optional<ErrorMapping> malformedBody(Throwable throwable) {
        return cast(throwable, ServerWebInputException.class)
                .map(ex -> new ErrorMapping(HttpStatus.BAD_REQUEST,
                        ErrorResponse.of(INVALID_REQUEST, "Malformed or missing request body")));
    }

    private Optional<ErrorMapping> business(Throwable throwable) {
        return cast(throwable, BusinessException.class)
                .map(ex -> new ErrorMapping(
                        BUSINESS_STATUS.getOrDefault(ex.getClass(), HttpStatus.UNPROCESSABLE_CONTENT),
                        ErrorResponse.of(ex.getCode().getKey(), ex.getMessage())));
    }

    private Optional<ErrorMapping> responseStatus(Throwable throwable) {
        return cast(throwable, ResponseStatusException.class)
                .map(ex -> new ErrorMapping(HttpStatus.valueOf(ex.getStatusCode().value()),
                        ErrorResponse.of(String.valueOf(ex.getStatusCode().value()), ex.getReason())));
    }

    private ErrorMapping unexpected(Throwable throwable) {
        return new ErrorMapping(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorResponse.of(INTERNAL_ERROR, throwable.getMessage()));
    }

    private <T> Optional<T> cast(Throwable throwable, Class<T> type) {
        return Optional.of(throwable)
                .filter(type::isInstance)
                .map(type::cast);
    }
}
