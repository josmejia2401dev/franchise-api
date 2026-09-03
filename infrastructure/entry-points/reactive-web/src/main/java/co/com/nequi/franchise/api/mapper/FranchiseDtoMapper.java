package co.com.nequi.franchise.api.mapper;

import co.com.nequi.franchise.api.dto.BranchResponse;
import co.com.nequi.franchise.api.dto.BranchTopProductResponse;
import co.com.nequi.franchise.api.dto.FranchiseResponse;
import co.com.nequi.franchise.api.dto.ProductResponse;
import co.com.nequi.franchise.model.franchise.BranchTopProduct;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.branch.Branch;
import co.com.nequi.franchise.model.franchise.product.Product;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FranchiseDtoMapper {

    FranchiseResponse toResponse(Franchise franchise);

    BranchResponse toResponse(Branch branch);

    ProductResponse toResponse(Product product);

    BranchTopProductResponse toResponse(BranchTopProduct topProduct);

    List<BranchResponse> toBranchResponses(List<Branch> branches);

    List<ProductResponse> toProductResponses(List<Product> products);
}
