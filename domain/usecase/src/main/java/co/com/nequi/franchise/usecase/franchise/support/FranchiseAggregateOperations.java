package co.com.nequi.franchise.usecase.franchise.support;

import co.com.nequi.franchise.model.franchise.branch.Branch;
import co.com.nequi.franchise.model.exception.NotFoundException;
import co.com.nequi.franchise.model.exception.ValidationException;
import co.com.nequi.franchise.model.franchise.Franchise;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class FranchiseAggregateOperations {

    public Mono<Branch> requireBranch(Franchise franchise, String branchId) {
        return Flux.fromIterable(franchise.getBranches())
                .filter(branch -> branch.getId().equals(branchId))
                .next()
                .switchIfEmpty(Mono.error(NotFoundException.branch(branchId)));
    }

    public Mono<Franchise> replaceBranch(Franchise franchise, String branchId, Branch updatedBranch) {
        return branchesReplacing(franchise, branchId, updatedBranch)
                .map(branches -> franchise.toBuilder().clearBranches().branches(branches).build());
    }

    private Mono<List<Branch>> branchesReplacing(Franchise franchise, String branchId, Branch updatedBranch) {
        return Flux.fromIterable(franchise.getBranches())
                .concatMap(branch -> Mono.just(branch)
                        .filter(current -> current.getId().equals(branchId))
                        .map(current -> updatedBranch)
                        .defaultIfEmpty(branch))
                .collectList();
    }

    public Mono<String> validName(String name) {
        return Mono.justOrEmpty(name)
                .map(String::trim)
                .filter(trimmed -> !trimmed.isEmpty())
                .switchIfEmpty(Mono.error(ValidationException.invalidName()));
    }
}
