package hipravin.jarvis.bookstore.load;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@SpringBootTest
@EmbeddedKafka
@ActiveProfiles(profiles = {"test"})
class BookstorePropertiesTest {

    @Autowired
    private BookstoreProperties bookstoreProperties;

    @Test
    void testPropertiesLoad() {
        assertNotNull(bookstoreProperties);
        assertNotNull(bookstoreProperties.loaderRootPath());
    }


}