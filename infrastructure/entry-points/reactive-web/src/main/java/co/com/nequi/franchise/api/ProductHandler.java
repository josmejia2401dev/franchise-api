package co.com.nequi.franchise.api;

import co.com.nequi.franchise.api.dto.AddProductRequest;
import co.com.nequi.franchise.api.dto.NameRequest;
import co.com.nequi.franchise.api.dto.UpdateStockRequest;
import co.com.nequi.franchise.api.mapper.FranchiseDtoMapper;
import co.com.nequi.franchise.api.response.FranchiseResponses;
import co.com.nequi.franchise.api.validation.PathVariables;
import co.com.nequi.franchise.api.validation.RequestValidator;
import co.com.nequi.franchise.model.franchise.port.in.ProductPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final ProductPort productPort;
    private final RequestValidator requestValidator;
    private final FranchiseResponses responses;
    private final FranchiseDtoMapper mapper;

    /** POST /api/v1/franchises/{franchiseId}/branches/{branchId}/products */
    public Mono<ServerResponse> add(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        return PathVariables.requireNonBlank(franchiseId, "franchiseId")
                .then(PathVariables.requireNonBlank(branchId, "branchId"))
                .then(request.bodyToMono(AddProductRequest.class))
                .flatMap(requestValidator::validate)
                .flatMap(body -> productPort.add(franchiseId, branchId, body.name(), body.stock()))
                .doOnSuccess(franchise -> log.info("Product added franchiseId={} branchId={}", franchiseId, branchId))
                .flatMap(responses::ok);
    }

    /** DELETE /api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId} */
    public Mono<ServerResponse> remove(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        String productId = request.pathVariable("productId");
        return PathVariables.requireNonBlank(franchiseId, "franchiseId")
                .then(PathVariables.requireNonBlank(branchId, "branchId"))
                .then(PathVariables.requireNonBlank(productId, "productId"))
                .then(Mono.defer(() -> productPort.remove(franchiseId, branchId, productId)))
                .doOnSuccess(franchise -> log.info("Product removed franchiseId={} branchId={} productId={}",
                        franchiseId, branchId, productId))
                .flatMap(responses::ok);
    }

    /** PATCH /api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock */
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
                .doOnSuccess(franchise -> log.info("Product stock updated franchiseId={} branchId={} productId={}",
                        franchiseId, branchId, productId))
                .flatMap(responses::ok);
    }

    /** PATCH /api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name */
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
                .doOnSuccess(franchise -> log.info("Product renamed franchiseId={} branchId={} productId={}",
                        franchiseId, branchId, productId))
                .flatMap(responses::ok);
    }

    /** GET /api/v1/franchises/{franchiseId}/products/top-stock */
    public Mono<ServerResponse> topStockPerBranch(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return PathVariables.requireNonBlank(franchiseId, "franchiseId")
                .flatMapMany(productPort::topStockPerBranch)
                .map(mapper::toResponse)
                .collectList()
                .doOnSuccess(list -> log.info("Top stock query franchiseId={} branches={}", franchiseId, list.size()))
                .flatMap(body -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body));
    }
}
