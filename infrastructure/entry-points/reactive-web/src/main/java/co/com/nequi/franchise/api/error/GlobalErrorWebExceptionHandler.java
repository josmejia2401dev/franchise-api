package co.com.nequi.franchise.api.error;

import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;

@Order(-2)
@Component
public class GlobalErrorWebExceptionHandler implements WebExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger("co.com.nequi.franchise.observability.Operations");

    private final ErrorMapper errorMapper;
    private final ServerCodecConfigurer codecConfigurer;

    public GlobalErrorWebExceptionHandler(ErrorMapper errorMapper, ServerCodecConfigurer codecConfigurer) {
        this.errorMapper = errorMapper;
        this.codecConfigurer = codecConfigurer;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        ErrorMapping mapping = errorMapper.map(throwable);
        logUnhandledServerError(exchange, mapping, throwable);
        return ServerResponse.status(mapping.status())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapping.body())
                .flatMap(response -> response.writeTo(exchange, new ResponseContext()));
    }

    /**
     * Only server errors (5xx) are logged here: they are unhandled/unexpected failures.
     * Controlled client errors (4xx) are logged by the owning handler, so they are not
     * re-logged to avoid duplicate entries.
     */
    private void logUnhandledServerError(ServerWebExchange exchange, ErrorMapping mapping, Throwable throwable) {
        java.util.Optional.of(mapping)
                .filter(m -> m.status().is5xxServerError())
                .ifPresent(m -> LOG.error("event=api_operation result=SERVER_ERROR status={} method={} path={} code={}",
                        m.status().value(),
                        exchange.getRequest().getMethod().name(),
                        exchange.getRequest().getPath().value(),
                        m.body().code(),
                        throwable));
    }

    private class ResponseContext implements ServerResponse.Context {
        @Override
        public List<org.springframework.http.codec.HttpMessageWriter<?>> messageWriters() {
            return codecConfigurer.getWriters();
        }

        @Override
        public List<ViewResolver> viewResolvers() {
            return List.of();
        }
    }
}
