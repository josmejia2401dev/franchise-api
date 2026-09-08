package co.com.nequi.franchise.api.error;

import co.com.nequi.franchise.model.exception.NotFoundException;
import co.com.nequi.franchise.model.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorMapperTest {

    private final ErrorMapper mapper = new ErrorMapper();

    @Test
    void mapsInvalidRequestToBadRequest() {
        ErrorMapping mapping = mapper.map(InvalidRequestException.ofField("name", "must not be blank"));

        assertThat(mapping.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(mapping.body().code()).isEqualTo("INVALID_REQUEST");
        assertThat(mapping.body().violations()).hasSize(1);
    }

    @Test
    void mapsMalformedBodyToBadRequest() {
        ErrorMapping mapping = mapper.map(new ServerWebInputException("bad body"));

        assertThat(mapping.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(mapping.body().code()).isEqualTo("INVALID_REQUEST");
    }

    @Test
    void mapsNotFoundToNotFound() {
        ErrorMapping mapping = mapper.map(NotFoundException.franchise("f1"));

        assertThat(mapping.status()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(mapping.body().code()).isEqualTo("FRANCHISE_NOT_FOUND");
    }

    @Test
    void mapsValidationToBadRequest() {
        ErrorMapping mapping = mapper.map(ValidationException.invalidStock());

        assertThat(mapping.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(mapping.body().code()).isEqualTo("INVALID_STOCK");
    }

    @Test
    void mapsResponseStatusException() {
        ErrorMapping mapping = mapper.map(new ResponseStatusException(HttpStatus.CONFLICT, "conflict"));

        assertThat(mapping.status()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void mapsUnexpectedToInternalError() {
        ErrorMapping mapping = mapper.map(new RuntimeException("boom"));

        assertThat(mapping.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(mapping.body().code()).isEqualTo("INTERNAL_ERROR");
    }
}
