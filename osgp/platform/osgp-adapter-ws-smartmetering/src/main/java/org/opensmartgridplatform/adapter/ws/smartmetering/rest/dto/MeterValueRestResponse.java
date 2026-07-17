// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.smartmetering.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/** A single metering value with its unit. */
public record MeterValueRestResponse(
    @Schema(description = "Numeric meter value.", example = "12345.678") BigDecimal value,
    @Schema(description = "Unit of the meter value.", example = "KWH") String unit) {}
