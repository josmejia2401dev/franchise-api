package co.com.nequi.franchise.api;

import co.com.nequi.franchise.api.dto.NameRequest;
import co.com.nequi.franchise.api.response.FranchiseResponses;
import co.com.nequi.franchise.api.validation.PathVariables;
import co.com.nequi.franchise.api.validation.RequestValidator;
import co.com.nequi.franchise.model.franchise.port.in.FranchisePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private final FranchisePort franchisePort;
    private final RequestValidator requestValidator;
    private final FranchiseResponses responses;

    /** POST /api/v1/franchises */
    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(NameRequest.class)
                .flatMap(requestValidator::validate)
                .flatMap(body -> franchisePort.create(body.name()))
                .doOnSuccess(franchise -> log.info("Franchise created id={}", franchise.getId()))
                .flatMap(responses::created);
    }

    /** PATCH /api/v1/franchises/{franchiseId}/name */
    public Mono<ServerResponse> rename(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return PathVariables.requireNonBlank(franchiseId, "franchiseId")
                .then(request.bodyToMono(NameRequest.class))
                .flatMap(requestValidator::validate)
                .flatMap(body -> franchisePort.rename(franchiseId, body.name()))
                .doOnSuccess(franchise -> log.info("Franchise renamed id={}", franchise.getId()))
                .flatMap(responses::ok);
    }
}
