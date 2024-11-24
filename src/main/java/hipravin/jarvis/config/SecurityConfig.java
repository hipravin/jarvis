package hipravin.jarvis.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.csrf.*;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

@Configuration
public class SecurityConfig {
    private static final String SC_REQUEST_ATTR_KEY_ACTUATOR = RequestAttributeSecurityContextRepository.class.getName()
            .concat(".SPRING_SECURITY_CONTEXT").concat("ACTUATOR");

    private static final String ACTUATOR_AUTHORITY_NAME = "ACTUATOR";
//The AuthorizationFilter runs not just on every request, but on every dispatch. This means that the REQUEST dispatch needs authorization, but also FORWARDs, ERRORs, and INCLUDEs.

//    It’s more secure because even with static resources it’s important to write secure headers,
//    which Spring Security cannot do if the request is ignored.
//
//    In this past, this came with a performance tradeoff since the session was consulted by Spring Security on every request.
//    As of Spring Security 6, however, the session is no longer pinged unless required by the authorization rule.
//    Because the performance impact is now addressed, Spring Security recommends using at least permitAll for all requests.

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
        var requestAttrRepository = new RequestAttributeSecurityContextRepository(SC_REQUEST_ATTR_KEY_ACTUATOR);

        http.securityMatcher("/actuator/**")
                .securityContext(config -> config.securityContextRepository(requestAttrRepository))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .userDetailsService(actuatorUserDetailsService())
                .authorizeHttpRequests(requests -> requests.requestMatchers(EndpointRequest.to("info", "health")).permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority(ACTUATOR_AUTHORITY_NAME));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain fallbackFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**")
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
                .authorizeHttpRequests((requests) ->
                        requests
                                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                                .requestMatchers("/api/**").permitAll()
                                .requestMatchers("/", "/login", "/error").permitAll()
                                .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    static final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {
        private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
        private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
            this.xor.handle(request, response, csrfToken);
            csrfToken.get();
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            String headerValue = request.getHeader(csrfToken.getHeaderName());

            return (StringUtils.hasText(headerValue) ? this.plain : this.xor).resolveCsrfTokenValue(request, csrfToken);
        }
    }

    UserDetailsService actuatorUserDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password("{bcrypt}$2a$10$e.FByjmyWgR3r97UL4GG/O53NrYpnZ5rlpXFHmi5dDqrEa/CKmzyS") //aadmin
                        .authorities(ACTUATOR_AUTHORITY_NAME).build());
    }
}
