package co.com.nequi.franchise.api.error;

import co.com.nequi.franchise.api.dto.ErrorResponse;
import org.springframework.http.HttpStatus;

public record ErrorMapping(HttpStatus status, ErrorResponse body) {
}
