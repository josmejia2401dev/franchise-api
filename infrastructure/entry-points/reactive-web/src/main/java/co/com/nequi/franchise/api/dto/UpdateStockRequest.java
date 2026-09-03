package co.com.nequi.franchise.api.dto;

import jakarta.validation.constraints.PositiveOrZero;


public record UpdateStockRequest(

        @PositiveOrZero(message = "stock must be zero or positive")
        long stock
) {
}
