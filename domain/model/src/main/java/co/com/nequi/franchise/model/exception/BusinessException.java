package co.com.nequi.franchise.model.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final BusinessErrorCode code;

    public BusinessException(BusinessErrorCode code, String message) {
        super(message);
        this.code = code;
    }
}
