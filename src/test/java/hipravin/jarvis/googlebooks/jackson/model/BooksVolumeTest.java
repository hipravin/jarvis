package hipravin.jarvis.googlebooks.jackson.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooksVolumeTest {
    @Test
    void testParsePublishedFailed() {
        assertTrue(BooksVolume.tryParseDateYear("asdf").isEmpty());
        assertTrue(BooksVolume.tryParseDateYear("11-12-13").isEmpty());

        assertTrue(BooksVolume.tryParseYear("asdf").isEmpty());
        assertTrue(BooksVolume.tryParseYear("11-12-13").isEmpty());
    }

    @Test
    void testParsePublished() {
        assertEquals(2026, BooksVolume.tryParseDateYear("2026-10-17").orElseThrow());
        assertEquals(2026, BooksVolume.tryParseYear("2026").orElseThrow());
    }
}