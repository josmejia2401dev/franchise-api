package co.com.nequi.franchise.api;

import co.com.nequi.franchise.api.error.ErrorMapper;
import co.com.nequi.franchise.api.error.GlobalErrorWebExceptionHandler;
import co.com.nequi.franchise.api.mapper.FranchiseDtoMapperImpl;
import co.com.nequi.franchise.api.response.FranchiseResponses;
import co.com.nequi.franchise.api.validation.RequestValidator;
import co.com.nequi.franchise.model.exception.NotFoundException;
import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.model.franchise.port.in.BranchPort;
import co.com.nequi.franchise.model.franchise.port.in.FranchisePort;
import co.com.nequi.franchise.model.franchise.port.in.ProductPort;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FranchiseRouterTest {

    private FranchisePort franchisePort;
    private BranchPort branchPort;
    private ProductPort productPort;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        franchisePort = mock(FranchisePort.class);
        branchPort = mock(BranchPort.class);
        productPort = mock(ProductPort.class);

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        RequestValidator requestValidator = new RequestValidator(validator);
        FranchiseDtoMapperImpl mapper = new FranchiseDtoMapperImpl();
        FranchiseResponses responses = new FranchiseResponses(mapper);
        ErrorMapper errorMapper = new ErrorMapper();

        FranchiseHandler franchiseHandler = new FranchiseHandler(franchisePort, requestValidator, responses, errorMapper);
        BranchHandler branchHandler = new BranchHandler(branchPort, requestValidator, responses, errorMapper);
        ProductHandler productHandler = new ProductHandler(productPort, requestValidator, responses, mapper, errorMapper);

        RouterRest routerRest = new RouterRest();
        GlobalErrorWebExceptionHandler errorHandler =
                new GlobalErrorWebExceptionHandler(errorMapper, ServerCodecConfigurer.create());

        webTestClient = WebTestClient
                .bindToRouterFunction(routerRest.franchiseRoutes(franchiseHandler, branchHandler, productHandler))
                .handlerStrategies(org.springframework.web.reactive.function.server.HandlerStrategies.builder()
                        .exceptionHandler(errorHandler)
                        .build())
                .build();
    }

    @Test
    void createFranchiseReturns201() {
        when(franchisePort.create(eq("Nequi")))
                .thenReturn(Mono.just(Franchise.builder().id("f1").name("Nequi").build()));

        webTestClient.post().uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Nequi\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("f1")
                .jsonPath("$.name").isEqualTo("Nequi");
    }

    @Test
    void createFranchiseWithBlankNameReturns400() {
        webTestClient.post().uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"   \"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_REQUEST");
    }

    @Test
    void renameFranchiseNotFoundReturns404() {
        when(franchisePort.rename(eq("missing"), any()))
                .thenReturn(Mono.error(NotFoundException.franchise("missing")));

        webTestClient.patch().uri("/api/v1/franchises/missing/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"New\"}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("FRANCHISE_NOT_FOUND");
    }

    @Test
    void addProductWithNegativeStockReturns400() {
        webTestClient.post().uri("/api/v1/franchises/f1/branches/b1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Mug\",\"stock\":-5}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_REQUEST");
    }
}
