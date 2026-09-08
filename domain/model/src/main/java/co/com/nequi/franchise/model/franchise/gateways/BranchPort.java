package co.com.nequi.franchise.model.franchise.gateways;

import co.com.nequi.franchise.model.franchise.Franchise;
import reactor.core.publisher.Mono;

public interface BranchPort {

    Mono<Franchise> add(String franchiseId, String branchName);

    Mono<Franchise> rename(String franchiseId, String branchId, String newName);
}
