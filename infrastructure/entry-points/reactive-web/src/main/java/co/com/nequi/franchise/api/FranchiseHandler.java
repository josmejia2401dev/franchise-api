package co.com.nequi.franchise.api;

import co.com.nequi.franchise.api.dto.NameRequest;
import co.com.nequi.franchise.api.error.ErrorMapper;
import co.com.nequi.franchise.api.observability.ApiOperation;
import co.com.nequi.franchise.api.observability.OperationLogger;
import co.com.nequi.franchise.api.response.FranchiseResponses;
import co.com.nequi.franchise.api.validation.PathVariables;
import co.com.nequi.franchise.api.validation.RequestValidator;
import co.com.nequi.franchise.model.franchise.gateways.FranchisePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private final FranchisePort franchisePort;
    private final RequestValidator requestValidator;
    private final FranchiseResponses responses;
    private final ErrorMapper errorMapper;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(NameRequest.class)
                .flatMap(requestValidator::validate)
                .flatMap(body -> franchisePort.create(body.name()))
                .doOnSuccess(franchise -> OperationLogger.success(
                        ApiOperation.CREATE_FRANCHISE, HttpStatus.CREATED.value(), "franchiseId", franchise.getId()))
                .doOnError(error -> OperationLogger.controlledError(
                        ApiOperation.CREATE_FRANCHISE, errorMapper.map(error)))
                .flatMap(responses::created);
    }

    public Mono<ServerResponse> rename(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return PathVariables.requireNonBlank(franchiseId, "franchiseId")
                .then(request.bodyToMono(NameRequest.class))
                .flatMap(requestValidator::validate)
                .flatMap(body -> franchisePort.rename(franchiseId, body.name()))
                .doOnSuccess(franchise -> OperationLogger.success(
                        ApiOperation.RENAME_FRANCHISE, HttpStatus.OK.value(), "franchiseId", franchise.getId()))
                .doOnError(error -> OperationLogger.controlledError(
                        ApiOperation.RENAME_FRANCHISE, errorMapper.map(error)))
                .flatMap(responses::ok);
    }
}
