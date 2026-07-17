// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.smartmetering.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** REST request body to enqueue a GetActualMeterReads request for a smart meter. */
public record ActualMeterReadsRestRequest(
    @NotBlank
        @Schema(
            description = "Identification of the smart meter to read.",
            example = "E0051000000000001")
        String deviceIdentification) {}
