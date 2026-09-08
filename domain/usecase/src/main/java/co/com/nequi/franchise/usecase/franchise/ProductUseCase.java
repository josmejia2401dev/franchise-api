package co.com.nequi.franchise.usecase.franchise;

import co.com.nequi.franchise.model.exception.NotFoundException;
import co.com.nequi.franchise.model.exception.ValidationException;
import co.com.nequi.franchise.model.franchise.product.BranchTopProduct;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.branch.Branch;
import co.com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import co.com.nequi.franchise.model.franchise.gateways.ProductPort;
import co.com.nequi.franchise.model.franchise.product.Product;
import co.com.nequi.franchise.model.shared.gateways.IdGenerator;
import co.com.nequi.franchise.usecase.franchise.support.FranchiseAggregateOperations;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.function.BinaryOperator;

@RequiredArgsConstructor
public class ProductUseCase implements ProductPort {

    private final FranchiseAggregateOperations operations;
    private final FranchiseRepository franchiseRepository;
    private final IdGenerator idGenerator;

    @Override
    public Mono<Franchise> add(String franchiseId, String branchId, String productName, long stock) {
        return operations.validName(productName)
                .flatMap(name -> validStock(stock).thenReturn(name))
                .flatMap(name -> requireFranchise(franchiseId)
                        .flatMap(franchise -> operations.requireBranch(franchise, branchId)
                                .map(branch -> branch.toBuilder()
                                        .product(Product.builder()
                                                .id(idGenerator.newId())
                                                .name(name)
                                                .stock(stock)
                                                .build())
                                        .build())
                                .flatMap(updatedBranch -> operations.replaceBranch(franchise, branchId, updatedBranch))))
                .flatMap(franchiseRepository::save);
    }

    @Override
    public Mono<Franchise> remove(String franchiseId, String branchId, String productId) {
        return requireFranchise(franchiseId)
                .flatMap(franchise -> operations.requireBranch(franchise, branchId)
                        .flatMap(branch -> requireProduct(branch, productId)
                                .flatMap(product -> removeProductFromBranch(branch, productId)))
                        .flatMap(updatedBranch -> operations.replaceBranch(franchise, branchId, updatedBranch)))
                .flatMap(franchiseRepository::save);
    }

    @Override
    public Mono<Franchise> updateStock(String franchiseId, String branchId, String productId, long stock) {
        return validStock(stock)
                .then(requireFranchise(franchiseId))
                .flatMap(franchise -> operations.requireBranch(franchise, branchId)
                        .flatMap(branch -> requireProduct(branch, productId)
                                .flatMap(product -> replaceProductInBranch(branch, product.withStock(stock))))
                        .flatMap(updatedBranch -> operations.replaceBranch(franchise, branchId, updatedBranch)))
                .flatMap(franchiseRepository::save);
    }

    @Override
    public Mono<Franchise> rename(String franchiseId, String branchId, String productId, String newName) {
        return operations.validName(newName)
                .flatMap(name -> requireFranchise(franchiseId)
                        .flatMap(franchise -> operations.requireBranch(franchise, branchId)
                                .flatMap(branch -> requireProduct(branch, productId)
                                        .map(product -> product.withName(name))
                                        .flatMap(renamed -> replaceProductInBranch(branch, renamed)))
                                .flatMap(updatedBranch -> operations.replaceBranch(franchise, branchId, updatedBranch))))
                .flatMap(franchiseRepository::save);
    }

    @Override
    public Flux<BranchTopProduct> topStockPerBranch(String franchiseId) {
        return requireFranchise(franchiseId)
                .flatMapMany(franchise -> Flux.fromIterable(franchise.getBranches()))
                .flatMap(this::topProductOfBranch);
    }

    private Mono<Franchise> requireFranchise(String franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(NotFoundException.franchise(franchiseId)));
    }

    private Mono<BranchTopProduct> topProductOfBranch(Branch branch) {
        return Flux.fromIterable(branch.getProducts())
                .reduce(BinaryOperator.maxBy(Comparator.comparingLong(Product::getStock)))
                .map(product -> BranchTopProduct.of(branch, product));
    }

    private Mono<Product> requireProduct(Branch branch, String productId) {
        return Flux.fromIterable(branch.getProducts())
                .filter(product -> product.getId().equals(productId))
                .next()
                .switchIfEmpty(Mono.error(NotFoundException.product(productId)));
    }

    private Mono<Branch> replaceProductInBranch(Branch branch, Product updatedProduct) {
        return Flux.fromIterable(branch.getProducts())
                .concatMap(product -> Mono.just(product)
                        .filter(current -> current.getId().equals(updatedProduct.getId()))
                        .map(current -> updatedProduct)
                        .defaultIfEmpty(product))
                .collectList()
                .map(products -> branch.toBuilder().clearProducts().products(products).build());
    }

    private Mono<Branch> removeProductFromBranch(Branch branch, String productId) {
        return Flux.fromIterable(branch.getProducts())
                .filter(product -> !product.getId().equals(productId))
                .collectList()
                .map(products -> branch.toBuilder().clearProducts().products(products).build());
    }

    private Mono<Long> validStock(long stock) {
        return Mono.just(stock)
                .filter(value -> value >= 0)
                .switchIfEmpty(Mono.error(ValidationException.invalidStock()));
    }
}
