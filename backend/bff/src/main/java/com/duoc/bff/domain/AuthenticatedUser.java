package com.duoc.bff.domain;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

/**
 * Usuario autenticado derivado del JWT: rol de plataforma (PM/DEV/DIR) y email.
 * El factory {@link #from(Authentication)} concentra la lectura del token para
 * que los controllers no manipulen authorities ni claims directamente.
 */
public record AuthenticatedUser(UserRole role, String email) {

    private static final Set<String> ROLES_VALIDOS = Set.of("PM", "DEV", "DIR");

    /**
     * Construye el usuario a partir del contexto de seguridad. El rol sale de las
     * authorities (las pobla JwtAuthoritiesConverter); se toma la primera que
     * matchee con un rol de plataforma valido.
     */
    public static AuthenticatedUser from(Authentication auth) {
        var role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .filter(ROLES_VALIDOS::contains)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "usuario sin rol de plataforma (PM/DEV/DIR)"));
        return new AuthenticatedUser(UserRole.valueOf(role), emailFrom(auth));
    }

    /** Email del usuario desde el JWT; cae a preferred_username si no viene email. */
    private static String emailFrom(Authentication auth) {
        if (auth.getPrincipal() instanceof Jwt jwt) {
            var email = jwt.getClaimAsString("email");
            if (email != null && !email.isBlank()) return email;
            return jwt.getClaimAsString("preferred_username");
        }
        return null;
    }
}
