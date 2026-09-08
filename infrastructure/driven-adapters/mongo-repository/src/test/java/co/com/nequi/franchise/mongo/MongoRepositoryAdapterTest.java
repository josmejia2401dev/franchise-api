package co.com.nequi.franchise.mongo;

import co.com.nequi.franchise.model.franchise.Franchise;
import co.com.nequi.franchise.mongo.data.FranchiseDocument;
import co.com.nequi.franchise.mongo.mapper.FranchiseDocumentMapperImpl;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MongoRepositoryAdapterTest {

    private MongoDBRepository repository;
    private MongoRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(MongoDBRepository.class);
        adapter = new MongoRepositoryAdapter(
                repository,
                new FranchiseDocumentMapperImpl(),
                CircuitBreaker.ofDefaults("test"),
                TimeLimiter.ofDefaults("test"),
                Retry.ofDefaults("test"));
    }

    @Test
    void saveMapsAndPersists() {
        Franchise franchise = Franchise.builder().id("f1").name("Nequi").build();
        when(repository.save(any(FranchiseDocument.class)))
                .thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(adapter.save(franchise))
                .assertNext(saved -> {
                    org.assertj.core.api.Assertions.assertThat(saved.getId()).isEqualTo("f1");
                    org.assertj.core.api.Assertions.assertThat(saved.getName()).isEqualTo("Nequi");
                })
                .verifyComplete();
    }

    @Test
    void findByIdReturnsMappedFranchise() {
        when(repository.findById(eq("f1")))
                .thenReturn(Mono.just(FranchiseDocument.builder()
                        .id("f1").name("Nequi").branches(java.util.List.of()).build()));

        StepVerifier.create(adapter.findById("f1"))
                .assertNext(found -> org.assertj.core.api.Assertions.assertThat(found.getName()).isEqualTo("Nequi"))
                .verifyComplete();
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        when(repository.findById(eq("missing"))).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById("missing"))
                .verifyComplete();
    }
}
