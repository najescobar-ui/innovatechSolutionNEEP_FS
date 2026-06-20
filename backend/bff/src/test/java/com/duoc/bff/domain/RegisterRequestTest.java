package com.duoc.bff.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** La validacion del registro (incluido el perfil) vive en el DTO, no en el controller. */
class RegisterRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void validRequest_hasNoViolations() {
        var req = new RegisterRequest("Ana", "Diaz", "ana@x.cl", "11.111.111-1", "Secret.1", "DEV");
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void invalidRole_isRejected() {
        var req = new RegisterRequest("Ana", "Diaz", "ana@x.cl", "11.111.111-1", "Secret.1", "BOSS");
        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("role"));
    }

    @Test
    void blankRole_isRejected() {
        var req = new RegisterRequest("Ana", "Diaz", "ana@x.cl", "11.111.111-1", "Secret.1", "");
        assertThat(validator.validate(req))
                .anyMatch(v -> v.getPropertyPath().toString().equals("role"));
    }
}
