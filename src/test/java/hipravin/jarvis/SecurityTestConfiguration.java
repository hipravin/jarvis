package hipravin.jarvis;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class SecurityTestConfiguration {
    @Bean
    @Order(0)
    public SecurityFilterChain permitAllFilterChain(HttpSecurity http) {
        http.securityMatcher("/**")
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(requests -> requests
                        .anyRequest().permitAll());
        return http.build();
    }
}
