package co.com.nequi.franchise.api;

import co.com.nequi.franchise.api.dto.NameRequest;
import co.com.nequi.franchise.api.error.ErrorMapper;
import co.com.nequi.franchise.api.observability.ApiOperation;
import co.com.nequi.franchise.api.observability.OperationLogger;
import co.com.nequi.franchise.api.response.FranchiseResponses;
import co.com.nequi.franchise.api.validation.PathVariables;
import co.com.nequi.franchise.api.validation.RequestValidator;
import co.com.nequi.franchise.model.franchise.gateways.BranchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BranchHandler {

    private final BranchPort branchPort;
    private final RequestValidator requestValidator;
    private final FranchiseResponses responses;
    private final ErrorMapper errorMapper;

    public Mono<ServerResponse> add(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return PathVariables.requireNonBlank(franchiseId, "franchiseId")
                .then(request.bodyToMono(NameRequest.class))
                .flatMap(requestValidator::validate)
                .flatMap(body -> branchPort.add(franchiseId, body.name()))
                .doOnSuccess(franchise -> OperationLogger.success(
                        ApiOperation.ADD_BRANCH, HttpStatus.OK.value(), "franchiseId", franchise.getId()))
                .doOnError(error -> OperationLogger.controlledError(
                        ApiOperation.ADD_BRANCH, errorMapper.map(error)))
                .flatMap(responses::ok);
    }

    public Mono<ServerResponse> rename(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        return PathVariables.requireNonBlank(franchiseId, "franchiseId")
                .then(PathVariables.requireNonBlank(branchId, "branchId"))
                .then(request.bodyToMono(NameRequest.class))
                .flatMap(requestValidator::validate)
                .flatMap(body -> branchPort.rename(franchiseId, branchId, body.name()))
                .doOnSuccess(franchise -> OperationLogger.success(
                        ApiOperation.RENAME_BRANCH, HttpStatus.OK.value(), "branchId", branchId))
                .doOnError(error -> OperationLogger.controlledError(
                        ApiOperation.RENAME_BRANCH, errorMapper.map(error)))
                .flatMap(responses::ok);
    }
}
