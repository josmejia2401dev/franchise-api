package co.com.nequi.franchise.model.exception;

public class ValidationException extends BusinessException {

    public ValidationException(BusinessErrorCode code, String message) {
        super(code, message);
    }

    public static ValidationException invalidName() {
        return new ValidationException(BusinessErrorCode.INVALID_NAME,
                BusinessErrorCode.INVALID_NAME.getDefaultMessage());
    }

    public static ValidationException invalidStock() {
        return new ValidationException(BusinessErrorCode.INVALID_STOCK,
                BusinessErrorCode.INVALID_STOCK.getDefaultMessage());
    }

    public static ValidationException duplicateBranchName() {
        return new ValidationException(BusinessErrorCode.DUPLICATE_BRANCH_NAME,
                BusinessErrorCode.DUPLICATE_BRANCH_NAME.getDefaultMessage());
    }

    public static ValidationException duplicateProductName() {
        return new ValidationException(BusinessErrorCode.DUPLICATE_PRODUCT_NAME,
                BusinessErrorCode.DUPLICATE_PRODUCT_NAME.getDefaultMessage());
    }
}
