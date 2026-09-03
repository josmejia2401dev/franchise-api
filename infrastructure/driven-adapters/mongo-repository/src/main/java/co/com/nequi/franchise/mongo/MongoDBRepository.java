package co.com.nequi.franchise.mongo;

import co.com.nequi.franchise.mongo.data.FranchiseDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;

public interface MongoDBRepository
        extends ReactiveMongoRepository<FranchiseDocument, String>,
        ReactiveQueryByExampleExecutor<FranchiseDocument> {
}
