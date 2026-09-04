package co.com.nequi.franchise.api;

import co.com.nequi.franchise.api.dto.AddProductRequest;
import co.com.nequi.franchise.api.dto.ErrorResponse;
import co.com.nequi.franchise.api.dto.FranchiseResponse;
import co.com.nequi.franchise.api.dto.NameRequest;
import co.com.nequi.franchise.api.dto.UpdateStockRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class RouterRest {

    private static final String BASE_PATH = "/api/v1/franchises";

    @Bean
    @RouterOperations({
            @RouterOperation(path = BASE_PATH, method = RequestMethod.POST, beanClass = FranchiseHandler.class, beanMethod = "create",
                    operation = @Operation(operationId = "createFranchise", summary = "Create a franchise",
                            requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Franchise created", content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = BASE_PATH + "/{franchiseId}/name", method = RequestMethod.PATCH, beanClass = FranchiseHandler.class, beanMethod = "rename",
                    operation = @Operation(operationId = "renameFranchise", summary = "Update franchise name",
                            parameters = @io.swagger.v3.oas.annotations.Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                            requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Franchise renamed", content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = BASE_PATH + "/{franchiseId}/branches", method = RequestMethod.POST, beanClass = BranchHandler.class, beanMethod = "add",
                    operation = @Operation(operationId = "addBranch", summary = "Add a branch to a franchise",
                            parameters = @io.swagger.v3.oas.annotations.Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                            requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Branch added", content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = BASE_PATH + "/{franchiseId}/branches/{branchId}/name", method = RequestMethod.PATCH, beanClass = BranchHandler.class, beanMethod = "rename",
                    operation = @Operation(operationId = "renameBranch", summary = "Update branch name",
                            requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Branch renamed", content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = BASE_PATH + "/{franchiseId}/branches/{branchId}/products", method = RequestMethod.POST, beanClass = ProductHandler.class, beanMethod = "add",
                    operation = @Operation(operationId = "addProduct", summary = "Add a product to a branch",
                            requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AddProductRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Product added", content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Branch not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = BASE_PATH + "/{franchiseId}/branches/{branchId}/products/{productId}", method = RequestMethod.DELETE, beanClass = ProductHandler.class, beanMethod = "remove",
                    operation = @Operation(operationId = "removeProduct", summary = "Remove a product from a branch",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Product removed", content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = BASE_PATH + "/{franchiseId}/branches/{branchId}/products/{productId}/stock", method = RequestMethod.PATCH, beanClass = ProductHandler.class, beanMethod = "updateStock",
                    operation = @Operation(operationId = "updateProductStock", summary = "Update product stock",
                            requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = UpdateStockRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Stock updated", content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid stock", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = BASE_PATH + "/{franchiseId}/branches/{branchId}/products/{productId}/name", method = RequestMethod.PATCH, beanClass = ProductHandler.class, beanMethod = "rename",
                    operation = @Operation(operationId = "renameProduct", summary = "Update product name",
                            requestBody = @RequestBody(required = true, content = @Content(schema = @Schema(implementation = NameRequest.class))),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Product renamed", content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                                    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            })),
            @RouterOperation(path = BASE_PATH + "/{franchiseId}/products/top-stock", method = RequestMethod.GET, beanClass = ProductHandler.class, beanMethod = "topStockPerBranch",
                    operation = @Operation(operationId = "topStockPerBranch", summary = "Get the top-stock product per branch within a franchise",
                            parameters = @io.swagger.v3.oas.annotations.Parameter(name = "franchiseId", in = ParameterIn.PATH, required = true),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Top-stock product per branch"),
                                    @ApiResponse(responseCode = "404", description = "Franchise not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                            }))
    })
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
