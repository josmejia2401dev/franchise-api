package co.com.nequi.franchise.model.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessErrorCode {

    FRANCHISE_NOT_FOUND("FRANCHISE_NOT_FOUND", "The requested franchise does not exist"),
    BRANCH_NOT_FOUND("BRANCH_NOT_FOUND", "The requested branch does not exist"),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "The requested product does not exist"),
    BRANCH_HAS_NO_PRODUCTS("BRANCH_HAS_NO_PRODUCTS", "The branch has no products"),
    INVALID_STOCK("INVALID_STOCK", "The stock must be zero or a positive value"),
    INVALID_NAME("INVALID_NAME", "The name must not be null or blank"),
    DUPLICATE_BRANCH_NAME("DUPLICATE_BRANCH_NAME", "A branch with the same name already exists in the franchise"),
    DUPLICATE_PRODUCT_NAME("DUPLICATE_PRODUCT_NAME", "A product with the same name already exists in the branch");

    private final String key;
    private final String defaultMessage;
}
