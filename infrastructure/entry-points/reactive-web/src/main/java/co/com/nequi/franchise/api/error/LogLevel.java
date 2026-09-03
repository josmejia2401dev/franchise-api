package co.com.nequi.franchise.api.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public enum LogLevel {

    SERVER {
        @Override
        void write(String method, String path, int status, String code, String message, Throwable throwable) {
            LOG.error("Unhandled error {} {} -> {} {}", method, path, status, code, throwable);
        }
    },
    CLIENT {
        @Override
        void write(String method, String path, int status, String code, String message, Throwable throwable) {
            LOG.warn("Client error {} {} -> {} {} : {}", method, path, status, code, message);
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger("co.com.nequi.franchise.api.error.RequestErrors");

    abstract void write(String method, String path, int status, String code, String message, Throwable throwable);

    static LogLevel forStatus(HttpStatus status) {
        return status.is5xxServerError() ? SERVER : CLIENT;
    }
}
