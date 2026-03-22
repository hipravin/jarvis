package hipravin.jarvis.engine.model;

import org.junit.jupiter.api.Test;

import static hipravin.jarvis.engine.model.InformationSource.fromString;
import static org.junit.jupiter.api.Assertions.*;

class SearchProvilderTypeTest {
    @Test
    void testFromNull() {
        assertNull(fromString(null));
    }

    @Test
    void testFromValidGithub() {
        assertEquals(InformationSource.GITHUB, fromString("GH"));
        assertEquals(InformationSource.GITHUB, fromString("gh"));
        assertEquals(InformationSource.GITHUB, fromString("GITHUB"));
        assertEquals(InformationSource.GITHUB, fromString(InformationSource.GITHUB.name()));
    }

    @Test
    void testFromValidGoogleBooks() {
        assertEquals(InformationSource.GOOGLE_BOOKS, fromString("GB"));
        assertEquals(InformationSource.GOOGLE_BOOKS, fromString("gb"));
        assertEquals(InformationSource.GOOGLE_BOOKS, fromString("GOOGLE_BOOKS"));
        assertEquals(InformationSource.GOOGLE_BOOKS, fromString(InformationSource.GOOGLE_BOOKS.name()));
    }

    @Test
    void testFromValidStackExchange() {
        assertEquals(InformationSource.STACKEXCHANGE, fromString("SE"));
        assertEquals(InformationSource.STACKEXCHANGE, fromString("se"));
        assertEquals(InformationSource.STACKEXCHANGE, fromString("STACKEXCHANGE"));
        assertEquals(InformationSource.STACKEXCHANGE, fromString(InformationSource.STACKEXCHANGE.name()));
    }

    @Test
    void testFromInvalid() {
        assertThrows(IllegalArgumentException.class, () -> fromString(" undefined"));
    }
}