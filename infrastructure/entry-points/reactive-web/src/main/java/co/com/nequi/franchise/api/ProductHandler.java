package co.com.nequi.franchise.api;

import co.com.nequi.franchise.api.dto.AddProductRequest;
import co.com.nequi.franchise.api.dto.NameRequest;
import co.com.nequi.franchise.api.dto.UpdateStockRequest;
import co.com.nequi.franchise.api.error.ErrorMapper;
import co.com.nequi.franchise.api.mapper.FranchiseDtoMapper;
import co.com.nequi.franchise.api.observability.ApiOperation;
import co.com.nequi.franchise.api.observability.OperationLogger;
import co.com.nequi.franchise.api.response.FranchiseResponses;
import co.com.nequi.franchise.api.validation.PathVariables;
import co.com.nequi.franchise.api.validation.RequestValidator;
import co.com.nequi.franchise.model.franchise.port.in.ProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final ProductPort productPort;
    private final RequestValidator requestValidator;
    private final FranchiseResponses responses;
    private final FranchiseDtoMapper mapper;
    private final ErrorMapper errorMapper;

    public Mono<ServerResponse> add(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        return PathVariables.requireNonBlank(franchiseId, "franchiseId")
                .then(PathVariables.requireNonBlank(branchId, "branchId"))
                .then(request.bodyToMono(AddProductRequest.class))
                .flatMap(requestValidator::validate)
                .flatMap(body -> productPort.add(franchiseId, branchId, body.name(), body.stock()))
                .doOnSuccess(franchise -> OperationLogger.success(
                        ApiOperation.ADD_PRODUCT, HttpStatus.OK.value(), "branchId", branchId))
                .doOnError(error -> OperationLogger.controlledError(
                        ApiOperation.ADD_PRODUCT, errorMapper.map(error)))
                .flatMap(responses::ok);
    }

    public Mono<ServerResponse> remove(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("productId");
        return PathVariables.requireNonBlank(franchiseId, "franchiseId")
                .then(PathVariables.requireNonBlank(branchId, "branchId"))
                .then(PathVariables.requireNonBlank(productId, "productId"))
                .then(Mono.defer(() -> productPort.remove(franchiseId, branchId, productId)))
                .doOnSuccess(franchise -> OperationLogger.success(
                        ApiOperation.REMOVE_PRODUCT, HttpStatus.OK.value(), "productId", productId))
                .doOnError(error -> OperationLogger.controlledError(
                        ApiOperation.REMOVE_PRODUCT, errorMapper.map(error)))
                .flatMap(responses::ok);
    }

    public Mono<ServerResponse> updateStock(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("productId");
        return PathVariables.requireNonBlank(franchiseId, "franchiseId")
                .then(PathVariables.requireNonBlank(branchId, "branchId"))
                .then(PathVariables.requireNonBlank(productId, "productId"))
                .then(request.bodyToMono(UpdateStockRequest.class))
                .flatMap(requestValidator::validate)
                .flatMap(body -> productPort.updateStock(franchiseId, branchId, productId, body.stock()))
                .doOnSuccess(franchise -> OperationLogger.success(
                        ApiOperation.UPDATE_PRODUCT_STOCK, HttpStatus.OK.value(), "productId", productId))
                .doOnError(error -> OperationLogger.controlledError(
                        ApiOperation.UPDATE_PRODUCT_STOCK, errorMapper.map(error)))
                .flatMap(responses::ok);
    }

    public Mono<ServerResponse> rename(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("productId");
        return PathVariables.requireNonBlank(franchiseId, "franchiseId")
                .then(PathVariables.requireNonBlank(branchId, "branchId"))
                .then(PathVariables.requireNonBlank(productId, "productId"))
                .then(request.bodyToMono(NameRequest.class))
                .flatMap(requestValidator::validate)
                .flatMap(body -> productPort.rename(franchiseId, branchId, productId, body.name()))
                .doOnSuccess(franchise -> OperationLogger.success(
                        ApiOperation.RENAME_PRODUCT, HttpStatus.OK.value(), "productId", productId))
                .doOnError(error -> OperationLogger.controlledError(
                        ApiOperation.RENAME_PRODUCT, errorMapper.map(error)))
                .flatMap(responses::ok);
    }

    public Mono<ServerResponse> topStockPerBranch(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return PathVariables.requireNonBlank(franchiseId, "franchiseId")
                .flatMapMany(productPort::topStockPerBranch)
                .map(mapper::toResponse)
                .collectList()
                .doOnSuccess(list -> OperationLogger.success(
                        ApiOperation.TOP_STOCK_PER_BRANCH, HttpStatus.OK.value(), "franchiseId", franchiseId))
                .doOnError(error -> OperationLogger.controlledError(
                        ApiOperation.TOP_STOCK_PER_BRANCH, errorMapper.map(error)))
                .flatMap(body -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body));
    }
}
