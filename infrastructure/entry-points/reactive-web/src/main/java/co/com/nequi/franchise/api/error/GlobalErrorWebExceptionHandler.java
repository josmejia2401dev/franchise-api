package co.com.nequi.franchise.api.error;

import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.List;

@Order(-2)
@Component
public class GlobalErrorWebExceptionHandler implements WebExceptionHandler {

    private final ErrorMapper errorMapper;
    private final ServerCodecConfigurer codecConfigurer;

    public GlobalErrorWebExceptionHandler(ErrorMapper errorMapper, ServerCodecConfigurer codecConfigurer) {
        this.errorMapper = errorMapper;
        this.codecConfigurer = codecConfigurer;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        ErrorMapping mapping = errorMapper.map(throwable);
        logError(exchange, mapping, throwable);
        return ServerResponse.status(mapping.status())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapping.body())
                .flatMap(response -> response.writeTo(exchange, new ResponseContext()));
    }

    private void logError(ServerWebExchange exchange, ErrorMapping mapping, Throwable throwable) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        LogLevel.forStatus(mapping.status())
                .write(method, path, mapping.status().value(), mapping.body().code(), mapping.body().message(), throwable);
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
