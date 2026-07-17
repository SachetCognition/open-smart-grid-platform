// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.dto.da;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class GetPQValuesPeriodicRequestDto implements Serializable {
  private static final long serialVersionUID = 4776483459295815846L;

  private final ZonedDateTime from;
  private final ZonedDateTime to;

  public GetPQValuesPeriodicRequestDto(final ZonedDateTime from, final ZonedDateTime to) {
    this.from = from;
    this.to = to;
  }

  public ZonedDateTime getFrom() {
    return this.from;
  }

  public ZonedDateTime getTo() {
    return this.to;
  }
}
