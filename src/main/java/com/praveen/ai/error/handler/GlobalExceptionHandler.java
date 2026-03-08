package com.praveen.ai.error.handler;

import com.praveen.ai.error.DatabaseException;
import com.praveen.ai.error.LLMException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final String TIMESTAMP = "timestamp";
  private static final String IST_TIMEZONE = "Asia/Kolkata";

  @ExceptionHandler(LLMException.class)
  public final ProblemDetail handleLLMException(LLMException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    problemDetail.setTitle("LLM Error");
    problemDetail.setDetail(ex.getMessage());
    problemDetail.setProperty(TIMESTAMP, LocalDateTime.now(ZoneId.of(IST_TIMEZONE)));
    return problemDetail;
  }

  @ExceptionHandler(DatabaseException.class)
  public final ProblemDetail handleDatabaseException(DatabaseException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    problemDetail.setTitle("Database Error");
    problemDetail.setDetail(ex.getMessage());
    problemDetail.setProperty(TIMESTAMP, LocalDateTime.now(ZoneId.of(IST_TIMEZONE)));
    return problemDetail;
  }
}
