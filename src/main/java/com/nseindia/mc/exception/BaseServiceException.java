package com.nseindia.mc.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class BaseServiceException extends RuntimeException {
  private HttpStatus httpStatus;
  private String message;

  public BaseServiceException(final String message, final HttpStatus httpStatus) {
    super(message);
    this.message = message;
    this.httpStatus = httpStatus;
  }
}
