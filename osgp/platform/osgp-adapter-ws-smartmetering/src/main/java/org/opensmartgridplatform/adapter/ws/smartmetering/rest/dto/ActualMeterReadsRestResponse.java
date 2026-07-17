// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.smartmetering.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/** The actual meter reads result for a smart meter, fetched by correlation UID. */
public record ActualMeterReadsRestResponse(
    @Schema(description = "Correlation identifier of the original request.") String correlationUid,
    @Schema(description = "Identification of the smart meter.", example = "E0051000000000001")
        String deviceIdentification,
    @Schema(description = "Timestamp of the meter reads.") Instant logTime,
    MeterValueRestResponse activeEnergyImport,
    MeterValueRestResponse activeEnergyExport,
    MeterValueRestResponse activeEnergyImportTariffOne,
    MeterValueRestResponse activeEnergyImportTariffTwo,
    MeterValueRestResponse activeEnergyExportTariffOne,
    MeterValueRestResponse activeEnergyExportTariffTwo) {}
