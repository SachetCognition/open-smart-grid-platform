// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.smartmetering.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensmartgridplatform.adapter.ws.domain.entities.ResponseData;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.common.AsyncResponse;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.common.OsgpUnitType;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.monitoring.ActualMeterReadsResponse;
import org.opensmartgridplatform.adapter.ws.schema.smartmetering.monitoring.MeterValue;
import org.opensmartgridplatform.adapter.ws.shared.services.ResponseDataService;
import org.opensmartgridplatform.adapter.ws.smartmetering.application.mapping.MonitoringMapper;
import org.opensmartgridplatform.adapter.ws.smartmetering.application.services.RequestService;
import org.opensmartgridplatform.domain.core.valueobjects.smartmetering.ActualMeterReadsQuery;
import org.opensmartgridplatform.domain.core.valueobjects.smartmetering.MeterReads;
import org.opensmartgridplatform.shared.exceptionhandling.ComponentType;
import org.opensmartgridplatform.shared.exceptionhandling.UnknownCorrelationUidException;
import org.opensmartgridplatform.shared.infra.jms.ResponseMessageResultType;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class SmartMeteringMonitoringRestControllerTest {

  private static final String ORGANISATION = "test-org";
  private static final String DEVICE = "E0051000000000001";
  private static final String CORRELATION_UID = "test-correlation-id-1234";

  @Mock private RequestService requestService;
  @Mock private ResponseDataService responseDataService;
  @Mock private MonitoringMapper monitoringMapper;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    final SmartMeteringMonitoringRestController controller =
        new SmartMeteringMonitoringRestController(
            this.requestService, this.responseDataService, this.monitoringMapper);
    this.mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new RestExceptionHandler())
            .setMessageConverters(
                new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json().build()))
            .build();
  }

  @Test
  void postEnqueuesRequestAndReturnsCorrelationUid() throws Exception {
    final AsyncResponse asyncResponse = new AsyncResponse();
    asyncResponse.setCorrelationUid(CORRELATION_UID);
    asyncResponse.setDeviceIdentification(DEVICE);

    when(this.requestService.enqueueAndSendRequest(any(), any(ActualMeterReadsQuery.class)))
        .thenReturn(asyncResponse);

    this.mockMvc
        .perform(
            post("/smartmetering/monitoring/actual-meter-reads")
                .header("OrganisationIdentification", ORGANISATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deviceIdentification\":\"" + DEVICE + "\"}"))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.correlationUid").value(CORRELATION_UID))
        .andExpect(jsonPath("$.deviceIdentification").value(DEVICE));
  }

  @Test
  void getReturnsAsyncResultByCorrelationUid() throws Exception {
    final MeterReads meterReads = mock(MeterReads.class);
    final ResponseData responseData = mock(ResponseData.class);
    when(responseData.getResultType()).thenReturn(ResponseMessageResultType.OK);
    when(responseData.getDeviceIdentification()).thenReturn(DEVICE);
    when(responseData.getMessageData()).thenReturn(meterReads);

    when(this.responseDataService.get(
            eq(CORRELATION_UID), eq(MeterReads.class), eq(ComponentType.WS_SMART_METERING)))
        .thenReturn(responseData);
    when(this.monitoringMapper.map(eq(meterReads), eq(ActualMeterReadsResponse.class)))
        .thenReturn(sampleActualMeterReadsResponse());

    this.mockMvc
        .perform(
            get("/smartmetering/monitoring/actual-meter-reads/{correlationUid}", CORRELATION_UID)
                .header("OrganisationIdentification", ORGANISATION))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.correlationUid").value(CORRELATION_UID))
        .andExpect(jsonPath("$.deviceIdentification").value(DEVICE))
        .andExpect(jsonPath("$.activeEnergyImport.value").value(123.456))
        .andExpect(jsonPath("$.activeEnergyImport.unit").value("KWH"));
  }

  @Test
  void getReturnsAcceptedWhenResultNotYetAvailable() throws Exception {
    when(this.responseDataService.get(
            eq(CORRELATION_UID), eq(MeterReads.class), eq(ComponentType.WS_SMART_METERING)))
        .thenThrow(new UnknownCorrelationUidException(ComponentType.WS_SMART_METERING));

    this.mockMvc
        .perform(
            get("/smartmetering/monitoring/actual-meter-reads/{correlationUid}", CORRELATION_UID)
                .header("OrganisationIdentification", ORGANISATION))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.correlationUid").value(CORRELATION_UID))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  private static ActualMeterReadsResponse sampleActualMeterReadsResponse() throws Exception {
    final ActualMeterReadsResponse response = new ActualMeterReadsResponse();
    final XMLGregorianCalendar logTime =
        DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
    response.setLogTime(logTime);
    response.setActiveEnergyImport(meterValue(new BigDecimal("123.456")));
    response.setActiveEnergyExport(meterValue(new BigDecimal("0")));
    response.setActiveEnergyImportTariffOne(meterValue(new BigDecimal("100")));
    response.setActiveEnergyImportTariffTwo(meterValue(new BigDecimal("23.456")));
    response.setActiveEnergyExportTariffOne(meterValue(new BigDecimal("0")));
    response.setActiveEnergyExportTariffTwo(meterValue(new BigDecimal("0")));
    return response;
  }

  private static MeterValue meterValue(final BigDecimal value) {
    final MeterValue meterValue = new MeterValue();
    meterValue.setValue(value);
    meterValue.setUnit(OsgpUnitType.KWH);
    return meterValue;
  }
}
