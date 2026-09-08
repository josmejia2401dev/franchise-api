package co.com.nequi.franchise.model.franchise.product;

import co.com.nequi.franchise.model.franchise.branch.Branch;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class BranchTopProduct {

    String branchId;
    String branchName;
    Product product;

    public static BranchTopProduct of(Branch branch, Product product) {
        return BranchTopProduct.builder()
                .branchId(branch.getId())
                .branchName(branch.getName())
                .product(product)
                .build();
    }
}
