package co.com.nequi.franchise.mongo.mapper;

import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.branch.Branch;
import co.com.nequi.franchise.model.franchise.product.Product;
import co.com.nequi.franchise.mongo.data.BranchDocument;
import co.com.nequi.franchise.mongo.data.FranchiseDocument;
import co.com.nequi.franchise.mongo.data.ProductDocument;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FranchiseDocumentMapper {

    Franchise toDomain(FranchiseDocument document);

    FranchiseDocument toDocument(Franchise franchise);

    Branch toDomain(BranchDocument document);

    BranchDocument toDocument(Branch branch);

    Product toDomain(ProductDocument document);

    ProductDocument toDocument(Product product);

    List<Branch> toDomainBranches(List<BranchDocument> documents);

    List<BranchDocument> toDocumentBranches(List<Branch> branches);

    List<Product> toDomainProducts(List<ProductDocument> documents);

    List<ProductDocument> toDocumentProducts(List<Product> products);
}
