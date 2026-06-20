package com.duoc.bff.domain;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedUserTest {

    private Jwt jwt(String email, String preferredUsername) {
        var b = Jwt.withTokenValue("t").header("alg", "none").subject("u");
        if (email != null) b.claim("email", email);
        if (preferredUsername != null) b.claim("preferred_username", preferredUsername);
        return b.build();
    }

    @Test
    void from_resolvesRoleAndEmail() {
        var auth = new JwtAuthenticationToken(jwt("dev@x.cl", "devuser"),
                List.of(new SimpleGrantedAuthority("ROLE_DEV")));

        var user = AuthenticatedUser.from(auth);

        assertThat(user.role()).isEqualTo(UserRole.DEV);
        assertThat(user.email()).isEqualTo("dev@x.cl");
    }

    @Test
    void from_fallsBackToPreferredUsernameWhenNoEmail() {
        var auth = new JwtAuthenticationToken(jwt(null, "someuser"),
                List.of(new SimpleGrantedAuthority("ROLE_PM")));

        var user = AuthenticatedUser.from(auth);

        assertThat(user.role()).isEqualTo(UserRole.PM);
        assertThat(user.email()).isEqualTo("someuser");
    }

    @Test
    void from_forbiddenWhenNoPlatformRole() {
        var auth = new JwtAuthenticationToken(jwt("x@x.cl", null),
                List.of(new SimpleGrantedAuthority("ROLE_OTHER")));

        assertThatThrownBy(() -> AuthenticatedUser.from(auth))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void from_emailNullWhenPrincipalIsNotJwt() {
        var auth = new UsernamePasswordAuthenticationToken("user", "pw",
                List.of(new SimpleGrantedAuthority("ROLE_DIR")));

        var user = AuthenticatedUser.from(auth);

        assertThat(user.role()).isEqualTo(UserRole.DIR);
        assertThat(user.email()).isNull();
    }
}
