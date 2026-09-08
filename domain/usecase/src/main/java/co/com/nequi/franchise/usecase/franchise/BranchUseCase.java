package co.com.nequi.franchise.usecase.franchise;

import co.com.nequi.franchise.model.franchise.branch.Branch;
import co.com.nequi.franchise.model.exception.NotFoundException;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.franchise.model.franchise.port.in.BranchPort;
import co.com.nequi.franchise.model.shared.gateways.IdGenerator;
import co.com.nequi.franchise.usecase.franchise.support.FranchiseAggregateOperations;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class BranchUseCase implements BranchPort {

    private final FranchiseAggregateOperations operations;
    private final FranchiseRepository franchiseRepository;
    private final IdGenerator idGenerator;

    @Override
    public Mono<Franchise> add(String franchiseId, String branchName) {
        return operations.validName(branchName)
                .flatMap(name -> requireFranchise(franchiseId)
                        .map(franchise -> franchise.toBuilder()
                                .branch(Branch.builder()
                                        .id(idGenerator.newId())
                                        .name(name)
                                        .build())
                                .build()))
                .flatMap(franchiseRepository::save);
    }

    @Override
    public Mono<Franchise> rename(String franchiseId, String branchId, String newName) {
        return operations.validName(newName)
                .flatMap(name -> requireFranchise(franchiseId)
                        .flatMap(franchise -> operations.requireBranch(franchise, branchId)
                                .map(branch -> branch.withName(name))
                                .flatMap(renamed -> operations.replaceBranch(franchise, branchId, renamed))))
                .flatMap(franchiseRepository::save);
    }

    private Mono<Franchise> requireFranchise(String franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(NotFoundException.franchise(franchiseId)));
    }
}
