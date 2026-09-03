package co.com.nequi.franchise.api;

import co.com.nequi.franchise.api.dto.NameRequest;
import co.com.nequi.franchise.api.response.FranchiseResponses;
import co.com.nequi.franchise.api.validation.PathVariables;
import co.com.nequi.franchise.api.validation.RequestValidator;
import co.com.nequi.franchise.model.franchise.port.in.BranchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BranchHandler {

    private final BranchPort branchPort;
    private final RequestValidator requestValidator;
    private final FranchiseResponses responses;

    /** POST /api/v1/franchises/{franchiseId}/branches */
    public Mono<ServerResponse> add(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return PathVariables.requireNonBlank(franchiseId, "franchiseId")
                .then(request.bodyToMono(NameRequest.class))
                .flatMap(requestValidator::validate)
                .flatMap(body -> branchPort.add(franchiseId, body.name()))
                .doOnSuccess(franchise -> log.info("Branch added to franchise id={}", franchise.getId()))
                .flatMap(responses::ok);
    }

    /** PATCH /api/v1/franchises/{franchiseId}/branches/{branchId}/name */
    public Mono<ServerResponse> rename(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("branchId");
        return PathVariables.requireNonBlank(franchiseId, "franchiseId")
                .then(PathVariables.requireNonBlank(branchId, "branchId"))
                .then(request.bodyToMono(NameRequest.class))
                .flatMap(requestValidator::validate)
                .flatMap(body -> branchPort.rename(franchiseId, branchId, body.name()))
                .doOnSuccess(franchise -> log.info("Branch renamed franchiseId={} branchId={}", franchiseId, branchId))
                .flatMap(responses::ok);
    }
}
