package co.com.nequi.franchise.api.response;

import co.com.nequi.franchise.api.mapper.FranchiseDtoMapper;
import co.com.nequi.franchise.model.franchise.Franchise;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FranchiseResponses {

    private final FranchiseDtoMapper mapper;

    public Mono<ServerResponse> ok(Franchise franchise) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.toResponse(franchise));
    }

    public Mono<ServerResponse> created(Franchise franchise) {
        return ServerResponse.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.toResponse(franchise));
    }
}
