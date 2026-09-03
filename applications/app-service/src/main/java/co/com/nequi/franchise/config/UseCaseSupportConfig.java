package co.com.nequi.franchise.config;

import co.com.nequi.franchise.usecase.franchise.support.FranchiseAggregateOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseSupportConfig {

    @Bean
    public FranchiseAggregateOperations franchiseAggregateOperations() {
        return new FranchiseAggregateOperations();
    }
}
