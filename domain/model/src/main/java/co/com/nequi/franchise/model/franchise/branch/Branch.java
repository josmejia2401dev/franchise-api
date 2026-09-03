package co.com.nequi.franchise.model.franchise.branch;

import co.com.nequi.franchise.model.franchise.product.Product;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.With;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class Branch {

    @With
    String id;

    @With
    String name;

    @With
    @Singular
    List<Product> products;
}
