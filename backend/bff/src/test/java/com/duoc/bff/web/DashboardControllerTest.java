package com.duoc.bff.web;

import com.duoc.bff.domain.DashboardDto;
import com.duoc.bff.domain.UserRole;
import com.duoc.bff.service.DashboardDtoFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock DashboardDtoFactory factory;
    @InjectMocks DashboardController controller;

    private Jwt jwt(String email) {
        var b = Jwt.withTokenValue("t").header("alg", "none").subject("u");
        if (email != null) b.claim("email", email);
        else b.claim("preferred_username", "someuser");
        return b.build();
    }

    @Test
    void dashboard_resolvesRoleAndEmail() {
        var auth = new JwtAuthenticationToken(jwt("dev@x.cl"),
                List.of(new SimpleGrantedAuthority("ROLE_DEV")));
        var dto = new DashboardDto.DevDashboard("DEV", 0, 0, List.of());
        when(factory.create(UserRole.DEV, "dev@x.cl")).thenReturn(dto);

        assertThat(controller.dashboard(auth)).isEqualTo(dto);
    }

    @Test
    void dashboard_forbiddenWhenNoPlatformRole() {
        var auth = new JwtAuthenticationToken(jwt("x@x.cl"),
                List.of(new SimpleGrantedAuthority("ROLE_OTHER")));

        assertThatThrownBy(() -> controller.dashboard(auth))
                .isInstanceOf(ResponseStatusException.class);
    }
}
