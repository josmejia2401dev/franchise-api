package co.com.nequi.franchise.config;

import co.com.nequi.franchise.model.shared.gateways.IdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class IdGeneratorConfig {

    @Bean
    public IdGenerator idGenerator() {
        return () -> UUID.randomUUID().toString();
    }
}
