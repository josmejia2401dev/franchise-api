package co.com.nequi.franchise.api.observability;

import co.com.nequi.franchise.api.dto.FieldViolation;
import co.com.nequi.franchise.api.error.ErrorMapping;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.StringJoiner;

@UtilityClass
public class OperationLogger {

    private final Logger log = LoggerFactory.getLogger("co.com.nequi.franchise.observability.Operations");

    public void success(ApiOperation operation, int status, String detailKey, String detailValue) {
        log.info(baseLine(operation, OperationResult.SUCCESS, status)
                .add(kv(detailKey, detailValue))
                .toString());
    }

    public void controlledError(ApiOperation operation, ErrorMapping mapping) {
        java.util.Optional.of(mapping)
                .filter(m -> m.status().is4xxClientError())
                .ifPresent(m -> log.warn(baseLine(operation, OperationResult.CLIENT_ERROR, m.status().value())
                        .add(kv("code", m.body().code()))
                        .add(kv("violations", formatViolations(m.body().violations())))
                        .toString()));
    }

    private StringJoiner baseLine(ApiOperation operation, OperationResult result, int status) {
        return new StringJoiner(" ")
                .add(kv("event", "api_operation"))
                .add(kv("operation", operation.name()))
                .add(kv("result", result.name()))
                .add(kv("status", String.valueOf(status)));
    }

    private String formatViolations(List<FieldViolation> violations) {
        StringJoiner joiner = new StringJoiner(",");
        for (FieldViolation violation : nullSafe(violations)) {
            joiner.add(violation.field() + ":" + violation.message());
        }
        return joiner.toString();
    }

    private List<FieldViolation> nullSafe(List<FieldViolation> violations) {
        return violations == null ? List.of() : violations;
    }

    private String kv(String key, String value) {
        String safeValue = value == null ? "" : value;
        boolean needsQuotes = safeValue.contains(" ") || safeValue.contains(",");
        return needsQuotes ? key + "=\"" + safeValue + "\"" : key + "=" + safeValue;
    }
}
