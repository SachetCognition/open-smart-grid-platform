// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.smartmetering.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.extern.slf4j.Slf4j;
import org.opensmartgridplatform.adapter.ws.domain.entities.ResponseData;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.common.AsyncResponse;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.monitoring.ActualMeterReadsResponse;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.monitoring.MeterValue;
import org.opensmartgridplatform.adapter.ws.shared.services.ResponseDataService;
import org.opensmartgridplatform.adapter.ws.smartmetering.application.mapping.MonitoringMapper;
import org.opensmartgridplatform.adapter.ws.smartmetering.application.services.RequestService;
import org.opensmartgridplatform.adapter.ws.smartmetering.endpoints.RequestMessageMetadata;
import org.opensmartgridplatform.adapter.ws.smartmetering.rest.dto.ActualMeterReadsRestRequest;
import org.opensmartgridplatform.adapter.ws.smartmetering.rest.dto.ActualMeterReadsRestResponse;
import org.opensmartgridplatform.adapter.ws.smartmetering.rest.dto.AsyncStatusRestResponse;
import org.opensmartgridplatform.adapter.ws.smartmetering.rest.dto.EnqueueRestResponse;
import org.opensmartgridplatform.adapter.ws.smartmetering.rest.dto.MeterValueRestResponse;
import org.opensmartgridplatform.domain.core.valueobjects.DeviceFunction;
import org.opensmartgridplatform.domain.core.valueobjects.smartmetering.ActualMeterReadsQuery;
import org.opensmartgridplatform.domain.core.valueobjects.smartmetering.MeterReads;
import org.opensmartgridplatform.shared.exceptionhandling.ComponentType;
import org.opensmartgridplatform.shared.exceptionhandling.FunctionalException;
import org.opensmartgridplatform.shared.exceptionhandling.OsgpException;
import org.opensmartgridplatform.shared.exceptionhandling.TechnicalException;
import org.opensmartgridplatform.shared.exceptionhandling.UnknownCorrelationUidException;
import org.opensmartgridplatform.shared.infra.jms.MessageType;
import org.opensmartgridplatform.shared.infra.jms.ResponseMessageResultType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST/JSON facade for smart metering monitoring, exposed <em>alongside</em> the existing SOAP
 * endpoints. It reuses the same enqueue/retrieve service layer ({@link RequestService}, {@link
 * ResponseDataService}) and Orika mapping ({@link MonitoringMapper}) as {@code
 * SmartMeteringMonitoringEndpoint}, following the same asynchronous pattern: a POST enqueues a
 * request and returns a {@code correlationUid}; a GET fetches the result by that {@code
 * correlationUid}.
 */
@Slf4j
@RestController
@RequestMapping(path = "/smartmetering/monitoring", produces = "application/json")
@Tag(
    name = "Smart Metering Monitoring",
    description =
        "REST/JSON facade for smart metering monitoring, additive to the existing SOAP endpoints.")
public class SmartMeteringMonitoringRestController {

  private static final String ORGANISATION_IDENTIFICATION_HEADER = "OrganisationIdentification";

  private final RequestService requestService;
  private final ResponseDataService responseDataService;
  private final MonitoringMapper monitoringMapper;

  public SmartMeteringMonitoringRestController(
      final RequestService requestService,
      final ResponseDataService responseDataService,
      final MonitoringMapper monitoringMapper) {
    this.requestService = requestService;
    this.responseDataService = responseDataService;
    this.monitoringMapper = monitoringMapper;
  }

  @Operation(
      summary = "Enqueue a GetActualMeterReads request",
      description =
          "Enqueues an asynchronous request to read the actual meter values of a smart meter and "
              + "returns a correlation UID to fetch the result with.")
  @ApiResponses(
      @ApiResponse(
          responseCode = "202",
          description = "Request accepted and enqueued.",
          content = @Content(schema = @Schema(implementation = EnqueueRestResponse.class))))
  @PostMapping(path = "/actual-meter-reads", consumes = "application/json")
  public ResponseEntity<EnqueueRestResponse> enqueueActualMeterReads(
      @Parameter(description = "Identification of the organisation issuing the request.")
          @RequestHeader(ORGANISATION_IDENTIFICATION_HEADER)
          final String organisationIdentification,
      @Valid @RequestBody final ActualMeterReadsRestRequest request)
      throws FunctionalException {

    log.debug(
        "Incoming REST ActualMeterReads request for meter: {}.", request.deviceIdentification());

    final ActualMeterReadsQuery requestData = new ActualMeterReadsQuery(false);

    final RequestMessageMetadata requestMessageMetadata =
        RequestMessageMetadata.newBuilder()
            .withOrganisationIdentification(organisationIdentification)
            .withDeviceIdentification(request.deviceIdentification())
            .withDeviceFunction(DeviceFunction.REQUEST_ACTUAL_METER_DATA)
            .withMessageType(MessageType.REQUEST_ACTUAL_METER_DATA)
            .build();

    final AsyncResponse asyncResponse =
        this.requestService.enqueueAndSendRequest(requestMessageMetadata, requestData);

    return ResponseEntity.accepted()
        .body(
            new EnqueueRestResponse(
                asyncResponse.getCorrelationUid(), asyncResponse.getDeviceIdentification()));
  }

  @Operation(
      summary = "Fetch the GetActualMeterReads result",
      description =
          "Fetches the result of a previously enqueued GetActualMeterReads request by its "
              + "correlation UID. Returns 202 while the result is not yet available.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Result available.",
        content = @Content(schema = @Schema(implementation = ActualMeterReadsRestResponse.class))),
    @ApiResponse(
        responseCode = "202",
        description = "Result not yet available.",
        content = @Content(schema = @Schema(implementation = AsyncStatusRestResponse.class)))
  })
  @GetMapping(path = "/actual-meter-reads/{correlationUid}")
  public ResponseEntity<Object> getActualMeterReads(
      @Parameter(description = "Identification of the organisation issuing the request.")
          @RequestHeader(ORGANISATION_IDENTIFICATION_HEADER)
          final String organisationIdentification,
      @Parameter(description = "Correlation UID returned when the request was enqueued.")
          @PathVariable
          final String correlationUid)
      throws OsgpException {

    log.debug(
        "Incoming REST ActualMeterReads result fetch for correlationUid: {}.", correlationUid);

    final ResponseData responseData;
    try {
      responseData =
          this.responseDataService.get(
              correlationUid, MeterReads.class, ComponentType.WS_SMART_METERING);
    } catch (final UnknownCorrelationUidException e) {
      log.debug("No result yet for correlationUid: {}.", correlationUid);
      return ResponseEntity.accepted().body(new AsyncStatusRestResponse(correlationUid, "PENDING"));
    }

    if (ResponseMessageResultType.NOT_OK == responseData.getResultType()) {
      throw new TechnicalException(
          ComponentType.WS_SMART_METERING, "Retrieving the actual meter reads failed.", null);
    }

    final ActualMeterReadsResponse mapped =
        this.monitoringMapper.map(responseData.getMessageData(), ActualMeterReadsResponse.class);

    return ResponseEntity.ok(toRestResponse(correlationUid, responseData, mapped));
  }

  private static ActualMeterReadsRestResponse toRestResponse(
      final String correlationUid,
      final ResponseData responseData,
      final ActualMeterReadsResponse mapped) {
    return new ActualMeterReadsRestResponse(
        correlationUid,
        responseData.getDeviceIdentification(),
        toInstant(mapped.getLogTime()),
        toMeterValue(mapped.getActiveEnergyImport()),
        toMeterValue(mapped.getActiveEnergyExport()),
        toMeterValue(mapped.getActiveEnergyImportTariffOne()),
        toMeterValue(mapped.getActiveEnergyImportTariffTwo()),
        toMeterValue(mapped.getActiveEnergyExportTariffOne()),
        toMeterValue(mapped.getActiveEnergyExportTariffTwo()));
  }

  private static MeterValueRestResponse toMeterValue(final MeterValue meterValue) {
    if (meterValue == null) {
      return null;
    }
    return new MeterValueRestResponse(
        meterValue.getValue(), meterValue.getUnit() == null ? null : meterValue.getUnit().value());
  }

  private static Instant toInstant(final XMLGregorianCalendar calendar) {
    return calendar == null ? null : calendar.toGregorianCalendar().toInstant();
  }
}
