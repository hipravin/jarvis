package hipravin.jarvis.stackexchange.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class StackExchangePropertiesTest {
    @Autowired
    StackExchangeProperties properties;

    @Test
    void loaddedAndMapped() {
        assertNotNull(properties);
        assertEquals("https://api.stackexchange.com/2.3", properties.apiBaseUrl());
        assertEquals("relevance", properties.searchExcerptsParams().get("sort"));
    }

    @Configuration
    @EnableConfigurationProperties(StackExchangeProperties.class)
    public static class TestConfig {

    }
}