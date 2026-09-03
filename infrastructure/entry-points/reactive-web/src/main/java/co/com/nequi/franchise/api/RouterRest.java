package co.com.nequi.franchise.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class RouterRest {

    private static final String BASE_PATH = "/api/v1/franchises";

    @Bean
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler franchiseHandler,
                                                          BranchHandler branchHandler,
                                                          ProductHandler productHandler) {
        return RouterFunctions.route()
                // --- Franchises ---
                .POST(BASE_PATH, accept(MediaType.APPLICATION_JSON), franchiseHandler::create)
                .PATCH(BASE_PATH + "/{franchiseId}/name", accept(MediaType.APPLICATION_JSON), franchiseHandler::rename)
                // --- Branches ---
                .POST(BASE_PATH + "/{franchiseId}/branches", accept(MediaType.APPLICATION_JSON), branchHandler::add)
                .PATCH(BASE_PATH + "/{franchiseId}/branches/{branchId}/name", accept(MediaType.APPLICATION_JSON), branchHandler::rename)
                // --- Products ---
                .POST(BASE_PATH + "/{franchiseId}/branches/{branchId}/products", accept(MediaType.APPLICATION_JSON), productHandler::add)
                .DELETE(BASE_PATH + "/{franchiseId}/branches/{branchId}/products/{productId}", productHandler::remove)
                .PATCH(BASE_PATH + "/{franchiseId}/branches/{branchId}/products/{productId}/stock", accept(MediaType.APPLICATION_JSON), productHandler::updateStock)
                .PATCH(BASE_PATH + "/{franchiseId}/branches/{branchId}/products/{productId}/name", accept(MediaType.APPLICATION_JSON), productHandler::rename)
                // --- Queries ---
                .GET(BASE_PATH + "/{franchiseId}/products/top-stock", productHandler::topStockPerBranch)
                .build();
    }
}
