package hipravin.jarvis.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchEngineImplTest {

    @Test
    void testHighlight() {
        String text = "abc sampl. sam sample f SAMPL.sampl. !";
        String markBold = SearchEngineImpl.highlight(text, "sampl.", "<b>%s</b>"::formatted);
        assertEquals("abc <b>sampl.</b> sam sample f <b>SAMPL.</b><b>sampl.</b> !", markBold);
    }
}