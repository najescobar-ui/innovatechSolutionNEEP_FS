package com.duoc.bff.web;

import com.duoc.bff.domain.RegisterRequest;
import com.duoc.bff.service.KeycloakAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    KeycloakAdminService keycloak;

    @InjectMocks
    AuthController controller;

    @Test
    void register_validRole_delegatesAndReturnsCreated() {
        var req = new RegisterRequest("Ana", "Diaz", "ana@x.cl", "11.111.111-1", "secret", "DEV");

        var resp = controller.register(req);

        assertThat(resp).containsEntry("status", "created").containsEntry("role", "DEV");
        verify(keycloak).register(req);
    }

    @Test
    void register_invalidRole_rejected() {
        var req = new RegisterRequest("Ana", "Diaz", "ana@x.cl", "1-9", "secret", "BOSS");

        assertThatThrownBy(() -> controller.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Perfil");
        verify(keycloak, never()).register(any());
    }
}
