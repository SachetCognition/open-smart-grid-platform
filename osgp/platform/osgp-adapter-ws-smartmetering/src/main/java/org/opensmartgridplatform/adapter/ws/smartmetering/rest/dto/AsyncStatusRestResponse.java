// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.smartmetering.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Status returned while the asynchronous result is not yet available. */
public record AsyncStatusRestResponse(
    @Schema(description = "Correlation identifier of the original request.") String correlationUid,
    @Schema(description = "Status of the asynchronous request.", example = "PENDING")
        String status) {}
