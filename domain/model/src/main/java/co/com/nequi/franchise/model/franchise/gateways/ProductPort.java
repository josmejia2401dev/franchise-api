package co.com.nequi.franchise.model.franchise.gateways;

import co.com.nequi.franchise.model.franchise.product.BranchTopProduct;
import co.com.nequi.franchise.model.franchise.Franchise;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductPort {

    Mono<Franchise> add(String franchiseId, String branchId, String productName, long stock);

    Mono<Franchise> remove(String franchiseId, String branchId, String productId);

    Mono<Franchise> updateStock(String franchiseId, String branchId, String productId, long stock);

    Mono<Franchise> rename(String franchiseId, String branchId, String productId, String newName);

    Flux<BranchTopProduct> topStockPerBranch(String franchiseId);
}
