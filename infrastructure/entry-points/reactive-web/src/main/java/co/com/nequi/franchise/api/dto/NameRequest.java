package co.com.nequi.franchise.api.dto;

import jakarta.validation.constraints.NotBlank;

public record NameRequest(

        @NotBlank(message = "name must not be blank")
        String name
) {
}
