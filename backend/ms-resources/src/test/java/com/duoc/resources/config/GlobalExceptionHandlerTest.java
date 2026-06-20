package com.duoc.resources.config;

import com.duoc.resources.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest req = new MockHttpServletRequest("GET", "/resources/99");

    @Test
    void notFound_is404() {
        var r = handler.handleNotFound(new NotFoundException("Resource not found: 99"), req);
        assertThat(r.getStatusCode().value()).isEqualTo(404);
        assertThat(r.getBody().message()).contains("not found");
        assertThat(r.getBody().path()).isEqualTo("/resources/99");
    }

    @Test
    void illegalArgument_is400() {
        var r = handler.handleBadRequest(new IllegalArgumentException("bad"), req);
        assertThat(r.getStatusCode().value()).isEqualTo(400);
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
