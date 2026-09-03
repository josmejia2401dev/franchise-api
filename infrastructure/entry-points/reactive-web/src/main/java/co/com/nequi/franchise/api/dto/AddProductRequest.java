package co.com.nequi.franchise.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record AddProductRequest(

        @NotBlank(message = "name must not be blank")
        String name,

        @PositiveOrZero(message = "stock must be zero or positive")
        long stock
) {
}
