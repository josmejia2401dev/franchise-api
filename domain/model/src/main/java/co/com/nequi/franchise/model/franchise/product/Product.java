package co.com.nequi.franchise.model.franchise.product;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder(toBuilder = true)
public class Product {

    @With
    String id;

    @With
    String name;

    @With
    long stock;
}
