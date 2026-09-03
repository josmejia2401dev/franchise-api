package co.com.nequi.franchise.api.error;

import co.com.nequi.franchise.api.dto.FieldViolation;
import lombok.Getter;

import java.util.List;

@Getter
public class InvalidRequestException extends RuntimeException {

    private final transient List<FieldViolation> violations;

    public InvalidRequestException(String message, List<FieldViolation> violations) {
        super(message);
        this.violations = violations;
    }

    public static InvalidRequestException ofField(String field, String message) {
        return new InvalidRequestException("Invalid request", List.of(new FieldViolation(field, message)));
    }
}
