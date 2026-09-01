package ecom.user.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        return request;
    }

    @Test
    void handleNotFound_returns404WithPath() {
        ResponseEntity<ApiError> response = handler.handleNotFound(
                ResourceNotFoundException.forUser(5L), request("/api/users/5"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getPath()).isEqualTo("/api/users/5");
        assertThat(response.getBody().getFieldErrors()).isNull();
    }

    @Test
    void handleDuplicate_returns409ForDomainException() {
        ResponseEntity<ApiError> response = handler.handleDuplicate(
                DuplicateResourceException.email("a@b.com"), request("/api/users"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains("a@b.com");
    }

    @Test
    void handleDuplicate_returns409ForDataIntegrityViolation() {
        ResponseEntity<ApiError> response = handler.handleDuplicate(
                new DataIntegrityViolationException("constraint"), request("/api/users"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).doesNotContain("constraint");
    }

    @Test
    void handleValidation_returns400WithFieldErrors() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "createUserRequest");
        bindingResult.addError(new FieldError("createUserRequest", "email", "must be a valid email"));
        MethodParameter methodParameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class), 0);
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(ex, request("/api/users"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        List<ApiError.FieldValidationError> fieldErrors = response.getBody().getFieldErrors();
        assertThat(fieldErrors).extracting(ApiError.FieldValidationError::field).contains("email");
    }

    @Test
    void handleGeneric_returns500WithoutLeakingDetail() {
        ResponseEntity<ApiError> response = handler.handleGeneric(
                new IllegalStateException("boom"), request("/api/users"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).doesNotContain("boom");
    }

    @SuppressWarnings("unused")
    private void dummy(String value) {
    }
}
