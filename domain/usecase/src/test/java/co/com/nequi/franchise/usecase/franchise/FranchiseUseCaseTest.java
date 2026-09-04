package co.com.nequi.franchise.usecase.franchise;

import co.com.nequi.franchise.model.exception.NotFoundException;
import co.com.nequi.franchise.model.exception.ValidationException;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.franchise.usecase.franchise.support.FranchiseAggregateOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FranchiseUseCaseTest {

    private FranchiseRepository repository;
    private FranchiseUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(FranchiseRepository.class);
        useCase = new FranchiseUseCase(new FranchiseAggregateOperations(), repository);
    }

    @Test
    void createPersistsFranchiseWithTrimmedName() {
        when(repository.save(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(((Franchise) invocation.getArgument(0)).withId("f1")));

        StepVerifier.create(useCase.create("  Nequi  "))
                .assertNext(saved -> assertThat(saved.getId()).isEqualTo("f1"))
                .verifyComplete();

        ArgumentCaptor<Franchise> captor = ArgumentCaptor.forClass(Franchise.class);
        org.mockito.Mockito.verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Nequi");
    }

    @Test
    void createFailsWhenNameIsBlank() {
        StepVerifier.create(useCase.create("   "))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void renameUpdatesNameOfExistingFranchise() {
        Franchise existing = Franchise.builder().id("f1").name("Old").build();
        when(repository.findById("f1")).thenReturn(Mono.just(existing));
        when(repository.save(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.rename("f1", "New"))
                .assertNext(updated -> assertThat(updated.getName()).isEqualTo("New"))
                .verifyComplete();
    }

    @Test
    void renameFailsWhenFranchiseNotFound() {
        when(repository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.rename("missing", "New"))
                .expectError(NotFoundException.class)
                .verify();
    }
}
