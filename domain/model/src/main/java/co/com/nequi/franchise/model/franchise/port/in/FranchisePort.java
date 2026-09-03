package co.com.nequi.franchise.model.franchise.port.in;

import co.com.nequi.franchise.model.franchise.Franchise;
import reactor.core.publisher.Mono;

public interface FranchisePort {

    Mono<Franchise> create(String name);

    Mono<Franchise> rename(String franchiseId, String newName);
}
