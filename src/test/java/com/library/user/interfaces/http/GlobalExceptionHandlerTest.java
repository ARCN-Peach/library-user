package com.library.user.interfaces.http;

import static org.assertj.core.api.Assertions.assertThat;

import com.library.user.application.exception.ConflictException;
import com.library.user.application.exception.NotFoundException;
import com.library.user.application.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsKnownExceptionsToExpectedStatusCodes() {
        assertThat(handler.handleNotFound(new NotFoundException("missing")).getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(handler.handleConflict(new ConflictException("duplicate")).getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(handler.handleUnauthorized(new UnauthorizedException("bad creds")).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(handler.handleIllegalArgument(new IllegalArgumentException("bad input")).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void joinsValidationErrorsInSingleDetail() throws Exception {
        var target = new Object();
        var binding = new BeanPropertyBindingResult(target, "target");
        binding.addError(new FieldError("target", "email", "must be a well-formed email address"));
        binding.addError(new FieldError("target", "name", "must not be blank"));
        var exception = new MethodArgumentNotValidException(null, binding);

        var problem = handler.handleValidation(exception);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).contains("email: must be a well-formed email address");
        assertThat(problem.getDetail()).contains("name: must not be blank");
    }

    @Test
    void handlesRequestContractFailures() {
        var violation = new ConstraintViolationException("contract", Set.of());

        assertThat(handler.handleRequestContract(violation).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void hidesUnexpectedErrors() {
        var problem = handler.handleGeneric(new RuntimeException("boom"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getDetail()).isEqualTo("Internal server error");
    }
}
