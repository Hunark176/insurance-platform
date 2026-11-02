package de.hunar.insurance.shared.web;

import de.hunar.insurance.shared.domain.DomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException exception) {
        log.warn("Resource not found: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail handleMissingResource(NoResourceFoundException exception) {
        log.warn("No API endpoint or static resource matched the request");
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Requested resource was not found");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(AccessDeniedException exception) {
        log.warn("Authenticated user is not authorized to access this resource");
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access is denied");
    }

    @ExceptionHandler({DomainException.class, IllegalStateException.class, IllegalArgumentException.class})
    ProblemDetail handleDomainError(RuntimeException exception) {
        log.warn("Request rejected by a business rule: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail handleOptimisticLockingFailure(OptimisticLockingFailureException exception) {
        log.warn("Concurrent update rejected by optimistic locking", exception);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "The resource was changed by another request. Reload it and retry."
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidationFailure(MethodArgumentNotValidException exception) {
        String invalidFields = exception.getBindingResult().getFieldErrors().stream()
                .map(org.springframework.validation.FieldError::getField)
                .distinct()
                .sorted()
                .toList()
                .toString();
        log.warn("Request validation failed for fields={}", invalidFields);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ProblemDetail handleMethodValidationFailure(HandlerMethodValidationException exception) {
        log.warn("Request parameter validation failed: {}", exception.getStatusCode());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadableRequest(HttpMessageNotReadableException exception) {
        log.warn("Request body could not be parsed: {}", exception.getMostSpecificCause().getClass().getSimpleName());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request body is invalid");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpectedFailure(Exception exception) {
        log.error("Unhandled request failure", exception);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Use the request ID when contacting support."
        );
    }
}
