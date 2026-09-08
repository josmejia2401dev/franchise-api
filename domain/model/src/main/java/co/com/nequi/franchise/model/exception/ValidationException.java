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
}
