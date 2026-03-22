package hipravin.jarvis.stackexchange.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = StackExchangeDtoMapper.class)
class StackExchangeDtoMapperTest {

    @Autowired
    StackExchangeDtoMapper mapper;

    @Test
    void collapseNewLines() {
        String multiline = """
               start
               
               
               
               mid
               end""";

        String collapsed = mapper.collapseNewLInes(multiline);
        assertEquals("start\n\nmid\nend", collapsed);
    }



}