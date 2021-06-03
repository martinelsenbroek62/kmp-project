package com.nseindia.mc.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * Process baseException.
   *
   * @param ex
   * @return
   */
  @ExceptionHandler({BaseServiceException.class})
  public ResponseEntity<Object> handleServiceException(BaseServiceException ex) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    return new ResponseEntity(errorMessage(ex.getMessage()), headers, ex.getHttpStatus());
  }

  /**
   * Process badRequest exception.
   *
   * @param ex
   * @return
   */
  @ExceptionHandler({ConstraintViolationException.class})
  public ResponseEntity<Object> handleValidationError(ConstraintViolationException ex) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    return new ResponseEntity(errorMessage(ex.getMessage()), headers, HttpStatus.BAD_REQUEST);
  }

  /**
   * Process MultipartException exception.
   *
   * @param ex
   * @return
   */
  @ExceptionHandler({MultipartException.class})
  public ResponseEntity<Object> handleMultipartException(MultipartException ex) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    String message = ex.getMessage().split(";")[0];
    return new ResponseEntity(errorMessage(message), headers, HttpStatus.BAD_REQUEST);
  }

  /**
   * Process unknown exception.
   *
   * @param ex
   * @return
   */
  @ExceptionHandler({Throwable.class})
  public ResponseEntity<Object> handleUnknownError(Throwable ex) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    logger.error("system error", ex);
    return new ResponseEntity(
        errorMessage("Internal server error"), headers, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Process MissingRequestHeaderException exception.
   *
   * @param ex The MissingRequestHeaderException instance to handle
   * @return ResponseEntity<Object>
   */
  @ExceptionHandler({MissingRequestHeaderException.class})
  public ResponseEntity<Object> handleMissingRequestHeaderError(MissingRequestHeaderException ex) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    return new ResponseEntity(errorMessage(ex.getMessage()), headers, HttpStatus.BAD_REQUEST);
  }

  /**
   * Process invalid argument exception.
   *
   * @param ex
   * @param headers
   * @param status
   * @param request
   * @return
   */
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map((e) -> String.join(":", e.getField(), e.getDefaultMessage()))
            .collect(Collectors.joining("; "));
    return new ResponseEntity(errorMessage(message), headers, HttpStatus.BAD_REQUEST);
  }

  /**
   * Assemble response message.
   *
   * @param message
   * @return
   */
  private Map<String, String> errorMessage(final String message) {
    Map<String, String> body = new HashMap<>();
    body.put("message", message);
    return body;
  }
}
