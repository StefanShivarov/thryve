package bg.sofia.uni.fmi.webjava.backend.exception;

import bg.sofia.uni.fmi.webjava.backend.model.dto.MessageResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String INVALID_INPUT_MESSAGE = "Invalid input!";
    private static final String UNEXPECTED_EXCEPTION_MESSAGE = "An unexpected error occurred!";

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<MessageResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex
    ) {
        List<String> errorMessages = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .toList();

        return ResponseEntity
            .badRequest()
            .body(new MessageResponse(INVALID_INPUT_MESSAGE, errorMessages));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<MessageResponse> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException ex
    ) {
        String fieldName = ex.getName();
        String invalidValue = Optional.ofNullable(ex.getValue())
            .map(Object::toString)
            .orElse("null");

        return ResponseEntity
            .badRequest()
            .body(new MessageResponse(
                String.format("Invalid value provided for field '%s': '%s'!", fieldName, invalidValue)
            ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<MessageResponse> handleConstraintViolationException(
        ConstraintViolationException ex
    ) {
        List<String> errorMessages = ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .toList();

        return ResponseEntity
            .badRequest()
            .body(new MessageResponse(INVALID_INPUT_MESSAGE, errorMessages));
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<MessageResponse> handleEntityAlreadyExistsException(EntityAlreadyExistsException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(EnrollmentRequestAlreadyFinalizedException.class)
    public ResponseEntity<MessageResponse> handleExceptionsWithBadRequest(
        EnrollmentRequestAlreadyFinalizedException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<MessageResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleUnexpectedException(Exception ex) {
        log.error(ex.getClass().getName());
        log.info("Error: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new MessageResponse(UNEXPECTED_EXCEPTION_MESSAGE));
    }

}
