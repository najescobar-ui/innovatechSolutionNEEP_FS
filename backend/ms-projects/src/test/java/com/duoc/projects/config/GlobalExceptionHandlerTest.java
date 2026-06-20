package com.duoc.projects.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest req = new MockHttpServletRequest("GET", "/projects/1");

    @Test
    void illegalArgument_is400() {
        var r = handler.handleBadRequest(new IllegalArgumentException("bad arg"), req);
        assertThat(r.getStatusCode().value()).isEqualTo(400);
        assertThat(r.getBody().message()).isEqualTo("bad arg");
        assertThat(r.getBody().path()).isEqualTo("/projects/1");
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
