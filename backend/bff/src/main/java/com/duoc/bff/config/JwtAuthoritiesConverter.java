package com.duoc.bff.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Saca authorities del JWT desde realm_access.roles (formato Keycloak).
 * Si cambiamos de IdP (Auth0, Cognito, etc), este es el unico archivo
 * que hay que tocar — el resto del codigo trabaja con GrantedAuthority
 * estandar.
 */
public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        var realm = (Map<String, Object>) jwt.getClaims().get("realm_access");
        if (realm == null) return List.of();
        var roles = (List<String>) realm.get("roles");
        if (roles == null) return List.of();
        return roles.stream()
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
                .toList();
    }
}
