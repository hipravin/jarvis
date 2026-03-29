package hipravin.jarvis;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping(path = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> currentUserClaims(@AuthenticationPrincipal OidcUser principal) {
        return Map.of("username", principal.getClaimAsString("preferred_username"));
    }

    @GetMapping(path = "/claims", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> currentUserClaims(@AuthenticationPrincipal OidcUser principal,
                                                 Authentication authentication) {
        Objects.requireNonNull(principal);
        Objects.requireNonNull(authentication);

        var authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("name", principal.getName());
        userInfo.put("token", principal.getIdToken().getTokenValue());
        userInfo.put("claims", principal.getClaims());
        userInfo.put("authorities", authorities);

        return userInfo;
    }
}
