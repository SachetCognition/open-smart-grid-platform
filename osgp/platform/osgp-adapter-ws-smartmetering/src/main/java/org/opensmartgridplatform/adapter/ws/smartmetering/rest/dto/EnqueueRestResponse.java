// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.smartmetering.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response returned when an asynchronous request has been enqueued. The {@code correlationUid} can
 * be used to fetch the result once it becomes available.
 */
public record EnqueueRestResponse(
    @Schema(
            description = "Correlation identifier to fetch the asynchronous result with.",
            example = "test-org|||E0051000000000001|||20240101120000000")
        String correlationUid,
    @Schema(description = "Identification of the smart meter.", example = "E0051000000000001")
        String deviceIdentification) {}
