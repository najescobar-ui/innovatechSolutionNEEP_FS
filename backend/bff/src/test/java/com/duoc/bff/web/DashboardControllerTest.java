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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock DashboardDtoFactory factory;
    @InjectMocks DashboardController controller;

    @Test
    void dashboard_delegatesWithUserFromJwt() {
        var jwt = Jwt.withTokenValue("t").header("alg", "none").subject("u")
                .claim("email", "dev@x.cl").build();
        var auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DEV")));
        var dto = new DashboardDto.DevDashboard("DEV", 0, 0, List.of());
        when(factory.create(UserRole.DEV, "dev@x.cl")).thenReturn(dto);

        assertThat(controller.dashboard(auth)).isEqualTo(dto);
    }
}
