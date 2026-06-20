package com.duoc.bff.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthoritiesConverterTest {

    private final JwtAuthoritiesConverter converter = new JwtAuthoritiesConverter();

    private Jwt jwtWith(Object realmAccess) {
        var builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .subject("user");
        if (realmAccess != null) builder.claim("realm_access", realmAccess);
        else builder.claim("scope", "openid");
        return builder.build();
    }

    @Test
    void mapsRealmRolesToPrefixedAuthorities() {
        var jwt = jwtWith(Map.of("roles", List.of("PM", "DEV")));

        var authorities = converter.convert(jwt);

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_PM", "ROLE_DEV");
    }

    @Test
    void emptyWhenNoRealmAccess() {
        assertThat(converter.convert(jwtWith(null))).isEmpty();
    }

    @Test
    void emptyWhenNoRoles() {
        assertThat(converter.convert(jwtWith(Map.of("other", "x")))).isEmpty();
    }
}
