// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.tariffswitching.application.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.AnnotationMethodArgumentResolver;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.MessagePriority;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.OrganisationIdentification;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.SoapHeaderInterceptor;
import org.opensmartgridplatform.adapter.ws.shared.config.AbstractWebServiceConfig;
import org.opensmartgridplatform.adapter.ws.shared.config.Jaxb2MarshallerFactory;
import org.opensmartgridplatform.adapter.ws.tariffswitching.application.exceptionhandling.DetailSoapFaultMappingExceptionResolver;
import org.opensmartgridplatform.adapter.ws.tariffswitching.application.exceptionhandling.SoapFaultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MarshallingPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;

@Configuration
@PropertySource("classpath:osgp-adapter-ws-tariffswitching.properties")
@PropertySource(value = "file:${osgp/Global/config}", ignoreResourceNotFound = true)
@PropertySource(
    value = "file:${osgp/AdapterWsTariffSwitching/config}",
    ignoreResourceNotFound = true)
public class WebServiceConfig extends AbstractWebServiceConfig {

  private static final String
      PROPERTY_NAME_MARSHALLER_CONTEXT_PATH_TARIFF_SWITCHING_AD_HOC_MANAGEMENT =
          "jaxb2.marshaller.context.path.tariffswitching.adhocmanagement";
  private static final String
      PROPERTY_NAME_MARSHALLER_CONTEXT_PATH_TARIFF_SWITCHING_SCHEDULE_MANAGEMENT =
          "jaxb2.marshaller.context.path.tariffswitching.schedulemanagement";

  private static final Logger LOGGER = LoggerFactory.getLogger(WebServiceConfig.class);

  /**
   * Method for creating the Marshaller for schedule management.
   *
   * @return Jaxb2Marshaller
   */
  @Bean
  public Jaxb2Marshaller tariffSwitchingAdHocManagementMarshaller() {
    return Jaxb2MarshallerFactory.createMarshaller(
        this.environment.getRequiredProperty(
            PROPERTY_NAME_MARSHALLER_CONTEXT_PATH_TARIFF_SWITCHING_AD_HOC_MANAGEMENT));
  }

  /**
   * Method for creating the Marshalling Payload Method Processor for Tariff Switching schedule
   * management.
   *
   * @return MarshallingPayloadMethodProcessor
   */
  @Bean
  public MarshallingPayloadMethodProcessor
      tariffSwitchingAdHocManagementMarshallingPayloadMethodProcessor() {
    return Jaxb2MarshallerFactory.createMethodProcessor(
        this.tariffSwitchingAdHocManagementMarshaller());
  }

  /**
   * Method for creating the Marshaller for schedule management.
   *
   * @return Jaxb2Marshaller
   */
  @Bean
  public Jaxb2Marshaller tariffSwitchingScheduleManagementMarshaller() {
    return Jaxb2MarshallerFactory.createMarshaller(
        this.environment.getRequiredProperty(
            PROPERTY_NAME_MARSHALLER_CONTEXT_PATH_TARIFF_SWITCHING_SCHEDULE_MANAGEMENT));
  }

  /**
   * Method for creating the Marshalling Payload Method Processor for Tariff Switching schedule
   * management.
   *
   * @return MarshallingPayloadMethodProcessor
   */
  @Bean
  public MarshallingPayloadMethodProcessor
      tariffSwitchingScheduleManagementMarshallingPayloadMethodProcessor() {
    return Jaxb2MarshallerFactory.createMethodProcessor(
        this.tariffSwitchingScheduleManagementMarshaller());
  }

  /**
   * Method for creating the Default Method Endpoint Adapter.
   *
   * @return DefaultMethodEndpointAdapter
   */
  @Bean
  public DefaultMethodEndpointAdapter defaultMethodEndpointAdapter() {
    final DefaultMethodEndpointAdapter defaultMethodEndpointAdapter =
        new DefaultMethodEndpointAdapter();

    final List<MethodArgumentResolver> methodArgumentResolvers = new ArrayList<>();

    // TARIFFSWITCHING
    methodArgumentResolvers.add(
        this.tariffSwitchingAdHocManagementMarshallingPayloadMethodProcessor());
    methodArgumentResolvers.add(
        this.tariffSwitchingScheduleManagementMarshallingPayloadMethodProcessor());

    methodArgumentResolvers.add(
        new AnnotationMethodArgumentResolver(
            ORGANISATION_IDENTIFICATION_CONTEXT, OrganisationIdentification.class));
    methodArgumentResolvers.add(
        new AnnotationMethodArgumentResolver(MESSAGE_PRIORITY_HEADER, MessagePriority.class));
    defaultMethodEndpointAdapter.setMethodArgumentResolvers(methodArgumentResolvers);

    final List<MethodReturnValueHandler> methodReturnValueHandlers = new ArrayList<>();

    // TARIFF
    methodReturnValueHandlers.add(
        this.tariffSwitchingAdHocManagementMarshallingPayloadMethodProcessor());
    methodReturnValueHandlers.add(
        this.tariffSwitchingScheduleManagementMarshallingPayloadMethodProcessor());

    defaultMethodEndpointAdapter.setMethodReturnValueHandlers(methodReturnValueHandlers);

    return defaultMethodEndpointAdapter;
  }

  @Bean
  public DetailSoapFaultMappingExceptionResolver exceptionResolver() {
    LOGGER.debug("Creating Detail Soap Fault Mapping Exception Resolver Bean");
    final DetailSoapFaultMappingExceptionResolver exceptionResolver =
        new DetailSoapFaultMappingExceptionResolver(new SoapFaultMapper());
    exceptionResolver.setOrder(1);

    final Properties props = new Properties();
    props.put("org.opensmartgridplatform.shared.exceptionhandling.OsgpException", SERVER);
    props.put("org.opensmartgridplatform.shared.exceptionhandling.FunctionalException", SERVER);
    props.put("org.opensmartgridplatform.shared.exceptionhandling.TechnicalException", SERVER);
    props.put(
        "org.opensmartgridplatform.shared.exceptionhandling.ConnectionFailureException", SERVER);
    exceptionResolver.setExceptionMappings(props);
    return exceptionResolver;
  }

  @Bean
  public SoapHeaderInterceptor messagePriorityInterceptor() {
    return new SoapHeaderInterceptor(MESSAGE_PRIORITY_HEADER, MESSAGE_PRIORITY_HEADER);
  }
}
