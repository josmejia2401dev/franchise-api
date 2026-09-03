package co.com.nequi.franchise.mongo.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.mongodb.autoconfigure.MongoConnectionDetails;
import org.springframework.boot.ssl.SslBundles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MongoConfigTest {


    private MongoConfig mongoConfigUnderTest;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mongoConfigUnderTest = new MongoConfig();
    }

    @Test
    void dbSecretTest() {
        final MongoDBSecret result = mongoConfigUnderTest.dbSecret("uri");

        assertEquals("uri", result.getUri());
    }

    @Test
    void testMongoProperties() {
        MongoDBSecret secret = mock(MongoDBSecret.class);
        SslBundles sslBundles = mock(SslBundles.class);
        when(secret.getUri()).thenReturn("uri");

        MongoConnectionDetails result = mongoConfigUnderTest.mongoProperties(secret, sslBundles);

        assertNotNull(result);
    }
}
