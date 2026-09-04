package co.com.nequi.franchise.usecase.franchise;

import co.com.nequi.franchise.model.franchise.branch.Branch;
import co.com.nequi.franchise.model.exception.NotFoundException;
import co.com.nequi.franchise.model.exception.ValidationException;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.franchise.model.franchise.product.Product;
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

class ProductUseCaseTest {

    private FranchiseRepository repository;
    private IdGenerator idGenerator;
    private ProductUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(FranchiseRepository.class);
        idGenerator = mock(IdGenerator.class);
        useCase = new ProductUseCase(new FranchiseAggregateOperations(), repository, idGenerator);
    }

    private Franchise franchiseWithBranch(Branch branch) {
        return Franchise.builder().id("f1").name("Nequi").branch(branch).build();
    }

    @Test
    void addPlacesProductInBranch() {
        Franchise existing = franchiseWithBranch(Branch.builder().id("b1").name("Downtown").build());
        when(repository.findById("f1")).thenReturn(Mono.just(existing));
        when(idGenerator.newId()).thenReturn("p1");
        when(repository.save(any(Franchise.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(useCase.add("f1", "b1", "Mug", 50))
                .assertNext(saved -> {
                    Product product = saved.getBranches().get(0).getProducts().get(0);
                    assertThat(product.getName()).isEqualTo("Mug");
                    assertThat(product.getStock()).isEqualTo(50);
                })
                .verifyComplete();
    }

    @Test
    void addFailsWhenStockIsNegative() {
        StepVerifier.create(useCase.add("f1", "b1", "Mug", -1))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void removeDeletesProductFromBranch() {
        Branch branch = Branch.builder().id("b1").name("Downtown")
                .product(Product.builder().id("p1").name("Mug").stock(10).build())
                .build();
        when(repository.findById("f1")).thenReturn(Mono.just(franchiseWithBranch(branch)));
        when(repository.save(any(Franchise.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(useCase.remove("f1", "b1", "p1"))
                .assertNext(saved -> assertThat(saved.getBranches().get(0).getProducts()).isEmpty())
                .verifyComplete();
    }

    @Test
    void removeFailsWhenProductNotFound() {
        Branch branch = Branch.builder().id("b1").name("Downtown").build();
        when(repository.findById("f1")).thenReturn(Mono.just(franchiseWithBranch(branch)));

        StepVerifier.create(useCase.remove("f1", "b1", "missing"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void updateStockChangesProductStock() {
        Branch branch = Branch.builder().id("b1").name("Downtown")
                .product(Product.builder().id("p1").name("Mug").stock(10).build())
                .build();
        when(repository.findById("f1")).thenReturn(Mono.just(franchiseWithBranch(branch)));
        when(repository.save(any(Franchise.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(useCase.updateStock("f1", "b1", "p1", 99))
                .assertNext(saved -> assertThat(saved.getBranches().get(0).getProducts().get(0).getStock()).isEqualTo(99))
                .verifyComplete();
    }

    @Test
    void topStockPerBranchReturnsHighestStockProduct() {
        Branch branch = Branch.builder().id("b1").name("Downtown")
                .product(Product.builder().id("p1").name("Mug").stock(50).build())
                .product(Product.builder().id("p2").name("Bottle").stock(120).build())
                .build();
        when(repository.findById("f1")).thenReturn(Mono.just(franchiseWithBranch(branch)));

        StepVerifier.create(useCase.topStockPerBranch("f1"))
                .assertNext(top -> {
                    assertThat(top.getBranchId()).isEqualTo("b1");
                    assertThat(top.getProduct().getName()).isEqualTo("Bottle");
                    assertThat(top.getProduct().getStock()).isEqualTo(120);
                })
                .verifyComplete();
    }

    @Test
    void topStockPerBranchFailsWhenFranchiseNotFound() {
        when(repository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.topStockPerBranch("missing"))
                .expectError(NotFoundException.class)
                .verify();
    }
}
