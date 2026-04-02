package hipravin.jarvis.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.csrf.*;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


@Configuration
@Profile("secured")
public class SecurityConfig {
    private static final String SC_REQUEST_ATTR_KEY_ACTUATOR = RequestAttributeSecurityContextRepository.class.getName()
            .concat(".SPRING_SECURITY_CONTEXT.").concat("ACTUATOR");

    private static final String ACTUATOR_AUTHORITY_NAME = "ACTUATOR";
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${management.server.user}")
    private String actuatorUser;

    @Value("${management.server.password}")
    private String actuatorPassword;

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
        var requestAttrRepository = new RequestAttributeSecurityContextRepository(SC_REQUEST_ATTR_KEY_ACTUATOR);
        var publicEndpoints = EndpointRequest.to("info", "health");
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
    @Order(2)
    public SecurityFilterChain fallbackFilterChain(HttpSecurity http) throws Exception {
//        var userDetailsService = userDetailsService();

        http.securityMatcher("/**")
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/v1/bookstore/manage/**")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
                .authorizeHttpRequests((requests) ->
                        requests
                                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                                .requestMatchers("/api/**").authenticated()
//                                .requestMatchers("/", "/login", "/error").permitAll()
                                .anyRequest().authenticated())
                .oauth2Login(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {

                    OidcIdToken idToken = oidcUserAuthority.getIdToken();
//                    OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

                    if (idToken.getClaim("resource_access") instanceof Map<?, ?> resourceAccess
                            && resourceAccess.get("jarvisclient") instanceof Map<?, ?> clientRoles
                            && clientRoles.get("roles") instanceof List<?> roles) {

                        roles.forEach(r -> mappedAuthorities.add(
                                new SimpleGrantedAuthority(String.valueOf(r))));
                    }
//                    ((LinkedTreeMap) ((LinkedTreeMap) idToken.getClaim("resource_access")).get("jarvisclient")).get("roles")


                    // Map the claims found in idToken and/or userInfo
                    // to one or more GrantedAuthority's and add it to mappedAuthorities

                } else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {

                    Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();

                    // Map the attributes found in userAttributes
                    // to one or more GrantedAuthority's and add it to mappedAuthorities

                }
            });

            return mappedAuthorities;
        };
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        // ...

        log.debug(success.toString());
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        log.warn(failures.toString());
        // ...
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
                User.withUsername(actuatorUser).password(actuatorPassword)
                        .authorities(ACTUATOR_AUTHORITY_NAME).build());
    }
}
