package co.com.nequi.franchise.usecase.franchise;

import co.com.nequi.franchise.model.franchise.branch.Branch;
import co.com.nequi.franchise.model.exception.NotFoundException;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.franchise.model.shared.gateways.IdGenerator;
import co.com.nequi.franchise.usecase.franchise.support.FranchiseAggregateOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BranchUseCaseTest {

    private FranchiseRepository repository;
    private IdGenerator idGenerator;
    private BranchUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(FranchiseRepository.class);
        idGenerator = mock(IdGenerator.class);
        useCase = new BranchUseCase(new FranchiseAggregateOperations(), repository, idGenerator);
    }

    @Test
    void addAppendsBranchToFranchise() {
        Franchise existing = Franchise.builder().id("f1").name("Nequi").build();
        when(repository.findById("f1")).thenReturn(Mono.just(existing));
        when(idGenerator.newId()).thenReturn("b1");
        when(repository.save(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.add("f1", "Downtown"))
                .assertNext(saved -> {
                    assertThat(saved.getBranches()).hasSize(1);
                    assertThat(saved.getBranches().get(0).getName()).isEqualTo("Downtown");
                    assertThat(saved.getBranches().get(0).getId()).isEqualTo("b1");
                })
                .verifyComplete();
    }

    @Test
    void renameFailsWhenBranchNotFound() {
        Franchise existing = Franchise.builder().id("f1").name("Nequi").build();
        when(repository.findById("f1")).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.rename("f1", "missing", "New"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void renameUpdatesBranchName() {
        Franchise existing = Franchise.builder()
                .id("f1").name("Nequi")
                .branch(Branch.builder().id("b1").name("Old").build())
                .build();
        when(repository.findById("f1")).thenReturn(Mono.just(existing));
        when(repository.save(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.rename("f1", "b1", "New"))
                .assertNext(saved -> assertThat(saved.getBranches().get(0).getName()).isEqualTo("New"))
                .verifyComplete();
    }
}
