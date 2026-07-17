// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.smartmetering.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.opensmartgridplatform.shared.exceptionhandling.FunctionalException;
import org.opensmartgridplatform.shared.exceptionhandling.OsgpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Translates platform exceptions raised by the REST facade into JSON error responses. */
@Slf4j
@RestControllerAdvice(basePackages = "org.opensmartgridplatform.adapter.ws.smartmetering.rest")
public class RestExceptionHandler {

  @ExceptionHandler(FunctionalException.class)
  public ResponseEntity<ErrorResponse> handleFunctionalException(final FunctionalException e) {
    log.warn("Functional exception in REST facade: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(Instant.now(), e.getMessage()));
  }

  @ExceptionHandler(OsgpException.class)
  public ResponseEntity<ErrorResponse> handleOsgpException(final OsgpException e) {
    log.error("Technical exception in REST facade", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(Instant.now(), e.getMessage()));
  }

  /** Simple error body. */
  public record ErrorResponse(
      @Schema(description = "Time the error occurred.") Instant timestamp,
      @Schema(description = "Human readable error message.") String message) {}
}
