package co.com.nequi.franchise.mongo.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        mongoConfigUnderTest = new MongoConfig();
    }

    @Test
    void assemblesUriReplacingCredentialPlaceholders() {
        MongoDBSecret result = mongoConfigUnderTest.dbSecret(
                "mongodb+srv://{username}:{password}@host/franchise", "user", "pass");

        assertEquals("mongodb+srv://user:pass@host/franchise", result.getUri());
    }

    @Test
    void keepsUriUnchangedWhenTemplateHasNoPlaceholders() {
        MongoDBSecret result = mongoConfigUnderTest.dbSecret(
                "mongodb://localhost:27017/franchise", "", "");

        assertEquals("mongodb://localhost:27017/franchise", result.getUri());
    }

    @Test
    void buildsMongoConnectionDetailsFromSecret() {
        MongoDBSecret secret = mock(MongoDBSecret.class);
        SslBundles sslBundles = mock(SslBundles.class);
        when(secret.getUri()).thenReturn("mongodb://localhost:27017/franchise");

        MongoConnectionDetails result = mongoConfigUnderTest.mongoProperties(secret, sslBundles);

        assertNotNull(result);
    }
}
