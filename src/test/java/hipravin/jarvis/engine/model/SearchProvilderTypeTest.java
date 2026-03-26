package hipravin.jarvis.engine.model;

import org.junit.jupiter.api.Test;

import static hipravin.jarvis.engine.model.InformationSource.fromAlias;
import static org.junit.jupiter.api.Assertions.*;

class SearchProvilderTypeTest {
    @Test
    void testFromNull() {
        assertNull(fromAlias(null));
    }

    @Test
    void testFromValidGithub() {
        assertEquals(InformationSource.GITHUB, fromAlias("GH"));
        assertEquals(InformationSource.GITHUB, fromAlias("gh"));
        assertEquals(InformationSource.GITHUB, fromAlias("GITHUB"));
        assertEquals(InformationSource.GITHUB, fromAlias(InformationSource.GITHUB.name()));
    }

    @Test
    void testFromValidGoogleBooks() {
        assertEquals(InformationSource.GOOGLE_BOOKS, fromAlias("GB"));
        assertEquals(InformationSource.GOOGLE_BOOKS, fromAlias("gb"));
        assertEquals(InformationSource.GOOGLE_BOOKS, fromAlias("GOOGLE_BOOKS"));
        assertEquals(InformationSource.GOOGLE_BOOKS, fromAlias(InformationSource.GOOGLE_BOOKS.name()));
    }

    @Test
    void testFromValidStackExchange() {
        assertEquals(InformationSource.STACKEXCHANGE, fromAlias("SE"));
        assertEquals(InformationSource.STACKEXCHANGE, fromAlias("se"));
        assertEquals(InformationSource.STACKEXCHANGE, fromAlias("STACKEXCHANGE"));
        assertEquals(InformationSource.STACKEXCHANGE, fromAlias(InformationSource.STACKEXCHANGE.name()));
    }

    @Test
    void testFromInvalid() {
        assertThrows(IllegalArgumentException.class, () -> fromAlias(" undefined"));
    }
}