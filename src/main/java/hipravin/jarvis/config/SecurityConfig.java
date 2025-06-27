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

import static org.springframework.boot.autoconfigure.security.SecurityProperties.BASIC_AUTH_ORDER;

@Configuration
public class SecurityConfig {
    private static final String SC_REQUEST_ATTR_KEY_ACTUATOR = RequestAttributeSecurityContextRepository.class.getName()
            .concat(".SPRING_SECURITY_CONTEXT").concat("ACTUATOR");

    private static final String ACTUATOR_AUTHORITY_NAME = "ACTUATOR";
    private static final String BOOKSTORE_MANAGE_AUTHORITY = "BOOKSTORE_MANAGE";

    @Bean
    @Order(BASIC_AUTH_ORDER - 10)
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
        var requestAttrRepository = new RequestAttributeSecurityContextRepository(SC_REQUEST_ATTR_KEY_ACTUATOR);
        var publicEndpoints = EndpointRequest.to("info", "health");//TODO: 'info' - is it safe?
        var actuatorUserDetailService = actuatorUserDetailsService();

        http.securityMatcher(EndpointRequest.toAnyEndpoint())
                .securityContext(config -> config.securityContextRepository(requestAttrRepository))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .userDetailsService(actuatorUserDetailService)
                .authorizeHttpRequests(requests -> requests
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers(publicEndpoints).permitAll()
                        .anyRequest().hasAuthority(ACTUATOR_AUTHORITY_NAME));
        return http.build();
    }

    @Bean
    @Order(BASIC_AUTH_ORDER - 9)
    public SecurityFilterChain fallbackFilterChain(HttpSecurity http) throws Exception {
        var userDetailsService = userDetailsService();

        http.securityMatcher("/**")
                .csrf(csrf -> csrf
//                        .ignoringRequestMatchers("/api/v*/bookstore/manage/**")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests((requests) ->
                        requests
                                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                                .requestMatchers("/api/v*/bookstore/manage/**").hasAuthority(BOOKSTORE_MANAGE_AUTHORITY)
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

    UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername("support").password("{bcrypt}$2a$10$VfLRcQHsrAI1QahR18SUqOxE/v1r/MViWjQzkbSAXdGLVclSTDR6K") //ssupport
                        .authorities(BOOKSTORE_MANAGE_AUTHORITY).build(),
                User.withUsername("jarvis").password("{bcrypt}$2a$10$hr0o22U7HxOoc7CSWsrYHOIs0wg63aeT2BSrFK.Ujaq4U53Tc7EYS") //jjarvis
                        .build()
                );
    }

    UserDetailsService actuatorUserDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password("{bcrypt}$2a$10$e.FByjmyWgR3r97UL4GG/O53NrYpnZ5rlpXFHmi5dDqrEa/CKmzyS") //aadmin
                        .authorities(ACTUATOR_AUTHORITY_NAME).build());
    }
}
