// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.domain.core.validation.joda;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Future;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class FutureValidator implements ConstraintValidator<Future, ZonedDateTime> {

  @Override
  public void initialize(final Future constraintAnnotation) {
    // Empty Method
  }

  @Override
  public boolean isValid(final ZonedDateTime value, final ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    final ZonedDateTime checkDate =
        ZonedDateTime.now(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC);

    return !value.isBefore(checkDate);
  }
}
