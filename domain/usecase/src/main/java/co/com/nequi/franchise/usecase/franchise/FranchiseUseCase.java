package co.com.nequi.franchise.usecase.franchise;

import co.com.nequi.franchise.model.exception.NotFoundException;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.franchise.model.franchise.port.in.FranchisePort;
import co.com.nequi.franchise.usecase.franchise.support.FranchiseAggregateOperations;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class FranchiseUseCase implements FranchisePort {

    private final FranchiseAggregateOperations operations;
    private final FranchiseRepository franchiseRepository;

    @Override
    public Mono<Franchise> create(String name) {
        return operations.validName(name)
                .map(validName -> Franchise.builder().name(validName).build())
                .flatMap(franchiseRepository::save);
    }

    @Override
    public Mono<Franchise> rename(String franchiseId, String newName) {
        return operations.validName(newName)
                .flatMap(name -> requireFranchise(franchiseId)
                        .map(franchise -> franchise.withName(name)))
                .flatMap(franchiseRepository::save);
    }

    private Mono<Franchise> requireFranchise(String franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(NotFoundException.franchise(franchiseId)));
    }
}
