package iam.platform.admin.interfaces.rest.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import iam.platform.common.api.ApiResponse;
import iam.platform.common.model.exception.BusinessException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
                log.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());
                return ResponseEntity.status(ex.getHttpStatus())
                                .body(ApiResponse.error(ex.getHttpStatus(), ex.getMessage(), null));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Void>> handleValidationException(
                        MethodArgumentNotValidException ex) {
                List<ApiResponse.FieldError> errors = ex.getBindingResult().getFieldErrors()
                                .stream()
                                .map(fe -> ApiResponse.FieldError.builder().field(fe.getField())
                                                .message(fe.getDefaultMessage()).build())
                                .collect(Collectors.toList());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse
                                .error(HttpStatus.BAD_REQUEST.value(), "Validation Error", errors));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
                        AccessDeniedException ex) {
                log.warn("Access denied: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Forbidden", null));
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
                        AuthenticationException ex) {
                log.warn("Authentication failed: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse
                                .error(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null));
        }

        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(
                        UsernameNotFoundException ex) {
                log.warn("User not found: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse
                                .error(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
                        IllegalArgumentException ex) {
                log.warn("Illegal argument: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse
                                .error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
                        DataIntegrityViolationException ex) {
                log.error("Data integrity violation: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(HttpStatus.CONFLICT.value(),
                                                "Data integrity constraint violation", null));
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(
                        NoResourceFoundException ex) {
                log.warn("Resource not found: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse
                                .error(HttpStatus.NOT_FOUND.value(), "Resource not found", null));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
                log.error("Unexpected error", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                "Internal Server Error", null));
        }
}
