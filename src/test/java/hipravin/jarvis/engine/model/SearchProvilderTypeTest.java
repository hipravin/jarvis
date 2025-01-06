package hipravin.jarvis.engine.model;

import org.junit.jupiter.api.Test;

import static hipravin.jarvis.engine.model.SearchProviderType.fromString;
import static org.junit.jupiter.api.Assertions.*;

class SearchProvilderTypeTest {
    @Test
    void testFromNull() {
        assertNull(fromString(null));
    }

    @Test
    void testFromValidGithub() {
        assertEquals(SearchProviderType.GITHUB, fromString("GH"));
        assertEquals(SearchProviderType.GITHUB, fromString("gh"));
        assertEquals(SearchProviderType.GITHUB, fromString("GITHUB"));
        assertEquals(SearchProviderType.GITHUB, fromString(SearchProviderType.GITHUB.name()));
    }

    @Test
    void testFromValidGoogleBooks() {
        assertEquals(SearchProviderType.GOOGLE_BOOKS, fromString("GB"));
        assertEquals(SearchProviderType.GOOGLE_BOOKS, fromString("gb"));
        assertEquals(SearchProviderType.GOOGLE_BOOKS, fromString("GOOGLE_BOOKS"));
        assertEquals(SearchProviderType.GOOGLE_BOOKS, fromString(SearchProviderType.GOOGLE_BOOKS.name()));
    }

    @Test
    void testFromInvalid() {
        assertThrows(IllegalArgumentException.class, () -> fromString(" undefined"));
    }
}