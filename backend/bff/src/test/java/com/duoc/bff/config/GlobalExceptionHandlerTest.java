package com.duoc.bff.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest req = new MockHttpServletRequest("POST", "/auth/register");

    @Test
    void illegalArgument_is400() {
        var r = handler.handleBadRequest(new IllegalArgumentException("bad"), req);
        assertThat(r.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void downstream_propagatesStatus() {
        var r = handler.handleDownstream(HttpClientErrorException.create(
                HttpStatus.CONFLICT, "Conflict", null, null, null), req);
        assertThat(r.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void responseStatus_passesThrough() {
        var r = handler.handleStatus(new ResponseStatusException(HttpStatus.FORBIDDEN, "nope"), req);
        assertThat(r.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void generic_is500() {
        var r = handler.handleGeneric(new RuntimeException("boom"), req);
        assertThat(r.getStatusCode().value()).isEqualTo(500);
    }
}
