package co.com.nequi.franchise.mongo.mapper;

import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.branch.Branch;
import co.com.nequi.franchise.model.franchise.product.Product;
import co.com.nequi.franchise.mongo.data.BranchDocument;
import co.com.nequi.franchise.mongo.data.FranchiseDocument;
import co.com.nequi.franchise.mongo.data.ProductDocument;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FranchiseDocumentMapperTest {

    private final FranchiseDocumentMapper mapper = new FranchiseDocumentMapperImpl();

    @Test
    void mapsDomainToDocumentWithNestedBranchesAndProducts() {
        Franchise franchise = Franchise.builder()
                .id("f1").name("Nequi")
                .branch(Branch.builder().id("b1").name("Downtown")
                        .product(Product.builder().id("p1").name("Mug").stock(10).build())
                        .build())
                .build();

        FranchiseDocument document = mapper.toDocument(franchise);

        assertThat(document.getId()).isEqualTo("f1");
        assertThat(document.getName()).isEqualTo("Nequi");
        assertThat(document.getBranches()).hasSize(1);
        assertThat(document.getBranches().get(0).getName()).isEqualTo("Downtown");
        assertThat(document.getBranches().get(0).getProducts().get(0).getName()).isEqualTo("Mug");
        assertThat(document.getBranches().get(0).getProducts().get(0).getStock()).isEqualTo(10);
    }

    @Test
    void mapsDocumentToDomainWithNestedBranchesAndProducts() {
        FranchiseDocument document = FranchiseDocument.builder()
                .id("f1").name("Nequi")
                .branches(List.of(BranchDocument.builder()
                        .id("b1").name("Downtown")
                        .products(List.of(ProductDocument.builder().id("p1").name("Mug").stock(10).build()))
                        .build()))
                .build();

        Franchise franchise = mapper.toDomain(document);

        assertThat(franchise.getId()).isEqualTo("f1");
        assertThat(franchise.getName()).isEqualTo("Nequi");
        assertThat(franchise.getBranches()).hasSize(1);
        assertThat(franchise.getBranches().get(0).getName()).isEqualTo("Downtown");
        assertThat(franchise.getBranches().get(0).getProducts().get(0).getStock()).isEqualTo(10);
    }

    @Test
    void mapsFranchiseWithoutBranches() {
        Franchise franchise = Franchise.builder().id("f1").name("Nequi").build();

        FranchiseDocument document = mapper.toDocument(franchise);

        assertThat(document.getId()).isEqualTo("f1");
        assertThat(document.getName()).isEqualTo("Nequi");
    }
}
