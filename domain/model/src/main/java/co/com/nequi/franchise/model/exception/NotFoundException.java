package co.com.nequi.franchise.model.exception;

public class NotFoundException extends BusinessException {

    public NotFoundException(BusinessErrorCode code, String message) {
        super(code, message);
    }

    public static NotFoundException franchise(String franchiseId) {
        return new NotFoundException(BusinessErrorCode.FRANCHISE_NOT_FOUND,
                "Franchise not found: " + franchiseId);
    }

    public static NotFoundException branch(String branchId) {
        return new NotFoundException(BusinessErrorCode.BRANCH_NOT_FOUND,
                "Branch not found: " + branchId);
    }

    public static NotFoundException product(String productId) {
        return new NotFoundException(BusinessErrorCode.PRODUCT_NOT_FOUND,
                "Product not found: " + productId);
    }
}
