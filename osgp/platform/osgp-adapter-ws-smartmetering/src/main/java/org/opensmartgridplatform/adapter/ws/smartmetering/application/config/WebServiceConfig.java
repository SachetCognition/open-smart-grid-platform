// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.smartmetering.application.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.AnnotationMethodArgumentResolver;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.BypassRetry;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.MaxScheduleTime;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.MessageMetadata;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.MessagePriority;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.OrganisationIdentification;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.ResponseUrl;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.ScheduleTime;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.SoapHeaderInterceptor;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.SoapHeaderMessageMetadataInterceptor;
import org.opensmartgridplatform.adapter.ws.shared.config.AbstractWebServiceConfig;
import org.opensmartgridplatform.adapter.ws.shared.config.Jaxb2MarshallerFactory;
import org.opensmartgridplatform.adapter.ws.smartmetering.application.exceptionhandling.DetailSoapFaultMappingExceptionResolver;
import org.opensmartgridplatform.adapter.ws.smartmetering.application.exceptionhandling.SoapFaultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MarshallingPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;

@Configuration
@PropertySource("classpath:osgp-adapter-ws-smartmetering.properties")
@PropertySource(value = "file:${osgp/Global/config}", ignoreResourceNotFound = true)
@PropertySource(value = "file:${osgp/AdapterWsSmartMetering/config}", ignoreResourceNotFound = true)
public class WebServiceConfig extends AbstractWebServiceConfig {

  @Value("${jaxb2.marshaller.context.path.smartmetering.adhoc}")
  private String marshallerContextPathAdhoc;

  @Value("${jaxb2.marshaller.context.path.smartmetering.bundle}")
  private String marshallerContextPathBundle;

  @Value("${jaxb2.marshaller.context.path.smartmetering.common}")
  private String marshallerContextPathCommon;

  @Value("${jaxb2.marshaller.context.path.smartmetering.configuration}")
  private String marshallerContextPathConfiguration;

  @Value("${jaxb2.marshaller.context.path.smartmetering.installation}")
  private String marshallerContextPathInstallation;

  @Value("${jaxb2.marshaller.context.path.smartmetering.management}")
  private String marshallerContextPathManagement;

  @Value("${jaxb2.marshaller.context.path.smartmetering.monitoring}")
  private String marshallerContextPathMonitoring;

  private static final String MESSAGE_SCHEDULETIME_HEADER = "ScheduleTime";
  public static final String MESSAGE_MAXSCHEDULETIME_HEADER = "MaxScheduleTime";
  private static final String MESSAGE_RESPONSE_URL_HEADER = "ResponseUrl";
  private static final String BYPASS_RETRY_HEADER = "BypassRetry";

  private static final String MESSAGE_METADATA_CONTEXT_PROPERTY_NAME = "MessageMetadata";

  private static final Logger LOGGER = LoggerFactory.getLogger(WebServiceConfig.class);

  // Client WS code

  /**
   * Method for creating the Marshaller for smart metering management.
   *
   * @return Jaxb2Marshaller
   */
  @Bean
  public Jaxb2Marshaller smartMeteringManagementMarshaller() {
    return Jaxb2MarshallerFactory.createMarshaller(this.marshallerContextPathManagement);
  }

  /**
   * Method for creating the Marshalling Payload Method Processor for Smart Metering management.
   *
   * @return MarshallingPayloadMethodProcessor
   */
  @Bean
  public MarshallingPayloadMethodProcessor
      smartMeteringManagementMarshallingPayloadMethodProcessor() {
    return Jaxb2MarshallerFactory.createMethodProcessor(this.smartMeteringManagementMarshaller());
  }

  /**
   * Method for creating the Marshaller for smart metering common.
   *
   * @return Jaxb2Marshaller
   */
  @Bean
  public Jaxb2Marshaller smartMeteringCommonMarshaller() {
    return Jaxb2MarshallerFactory.createMarshaller(this.marshallerContextPathCommon);
  }

  /**
   * Method for creating the Marshaller for smart metering bundle.
   *
   * @return Jaxb2Marshaller
   */
  @Bean
  public Jaxb2Marshaller smartMeteringBundleMarshaller() {
    return Jaxb2MarshallerFactory.createMarshaller(this.marshallerContextPathBundle);
  }

  /**
   * Method for creating the Marshalling Payload Method Processor for Smart Metering bundle.
   *
   * @return MarshallingPayloadMethodProcessor
   */
  @Bean
  public MarshallingPayloadMethodProcessor smartMeteringBundleMarshallingPayloadMethodProcessor() {
    return Jaxb2MarshallerFactory.createMethodProcessor(this.smartMeteringBundleMarshaller());
  }

  /**
   * Method for creating the Marshalling Payload Method Processor for Smart Metering common.
   *
   * @return MarshallingPayloadMethodProcessor
   */
  @Bean
  public MarshallingPayloadMethodProcessor smartMeteringCommonMarshallingPayloadMethodProcessor() {
    return Jaxb2MarshallerFactory.createMethodProcessor(this.smartMeteringCommonMarshaller());
  }

  /**
   * Method for creating the Marshaller for smart metering installation.
   *
   * @return Jaxb2Marshaller
   */
  @Bean
  public Jaxb2Marshaller smartMeteringInstallationMarshaller() {
    return Jaxb2MarshallerFactory.createMarshaller(this.marshallerContextPathInstallation);
  }

  /**
   * Method for creating the Marshalling Payload Method Processor for Smart Metering installation.
   *
   * @return MarshallingPayloadMethodProcessor
   */
  @Bean
  public MarshallingPayloadMethodProcessor
      smartMeteringInstallationMarshallingPayloadMethodProcessor() {
    return Jaxb2MarshallerFactory.createMethodProcessor(this.smartMeteringInstallationMarshaller());
  }

  /**
   * Method for creating the Marshaller for smart metering monitoring.
   *
   * @return Jaxb2Marshaller
   */
  @Bean
  public Jaxb2Marshaller smartMeteringMonitoringMarshaller() {
    return Jaxb2MarshallerFactory.createMarshaller(this.marshallerContextPathMonitoring);
  }

  /**
   * Method for creating the Marshaller for smart metering adhoc.
   *
   * @return Jaxb2Marshaller
   */
  @Bean
  public Jaxb2Marshaller smartMeteringAdhocMarshaller() {
    return Jaxb2MarshallerFactory.createMarshaller(this.marshallerContextPathAdhoc);
  }

  /**
   * Method for creating the Marshalling Payload Method Processor for Smart Metering adhoc.
   *
   * @return MarshallingPayloadMethodProcessor
   */
  @Bean
  public MarshallingPayloadMethodProcessor smartMeteringAdhocMarshallingPayloadMethodProcessor() {
    return Jaxb2MarshallerFactory.createMethodProcessor(this.smartMeteringAdhocMarshaller());
  }

  /**
   * Method for creating the Marshaller for smart metering configuration.
   *
   * @return Jaxb2Marshaller
   */
  @Bean
  public Jaxb2Marshaller smartMeteringConfigurationMarshaller() {
    return Jaxb2MarshallerFactory.createMarshaller(this.marshallerContextPathConfiguration);
  }

  /**
   * Method for creating the Marshalling Payload Method Processor for Smart Metering configuration.
   *
   * @return MarshallingPayloadMethodProcessor
   */
  @Bean
  public MarshallingPayloadMethodProcessor
      smartMeteringConfigurationMarshallingPayloadMethodProcessor() {
    return Jaxb2MarshallerFactory.createMethodProcessor(
        this.smartMeteringConfigurationMarshaller());
  }

  /**
   * Method for creating the Marshalling Payload Method Processor for Smart Metering monitoring.
   *
   * @return MarshallingPayloadMethodProcessor
   */
  @Bean
  public MarshallingPayloadMethodProcessor
      smartMeteringMonitoringMarshallingPayloadMethodProcessor() {
    return Jaxb2MarshallerFactory.createMethodProcessor(this.smartMeteringMonitoringMarshaller());
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

    // SMART METERING
    methodArgumentResolvers.add(this.smartMeteringManagementMarshallingPayloadMethodProcessor());
    methodArgumentResolvers.add(this.smartMeteringBundleMarshallingPayloadMethodProcessor());
    methodArgumentResolvers.add(this.smartMeteringCommonMarshallingPayloadMethodProcessor());
    methodArgumentResolvers.add(this.smartMeteringInstallationMarshallingPayloadMethodProcessor());
    methodArgumentResolvers.add(this.smartMeteringMonitoringMarshallingPayloadMethodProcessor());
    methodArgumentResolvers.add(this.smartMeteringAdhocMarshallingPayloadMethodProcessor());
    methodArgumentResolvers.add(this.smartMeteringConfigurationMarshallingPayloadMethodProcessor());

    methodArgumentResolvers.add(
        new AnnotationMethodArgumentResolver(
            ORGANISATION_IDENTIFICATION_CONTEXT, OrganisationIdentification.class));
    methodArgumentResolvers.add(
        new AnnotationMethodArgumentResolver(MESSAGE_PRIORITY_HEADER, MessagePriority.class));
    methodArgumentResolvers.add(
        new AnnotationMethodArgumentResolver(MESSAGE_SCHEDULETIME_HEADER, ScheduleTime.class));
    methodArgumentResolvers.add(
        new AnnotationMethodArgumentResolver(
            MESSAGE_MAXSCHEDULETIME_HEADER, MaxScheduleTime.class, true));
    methodArgumentResolvers.add(
        new AnnotationMethodArgumentResolver(MESSAGE_RESPONSE_URL_HEADER, ResponseUrl.class));
    methodArgumentResolvers.add(
        new AnnotationMethodArgumentResolver(BYPASS_RETRY_HEADER, BypassRetry.class));
    methodArgumentResolvers.add(
        new AnnotationMethodArgumentResolver(
            MESSAGE_METADATA_CONTEXT_PROPERTY_NAME, MessageMetadata.class));
    defaultMethodEndpointAdapter.setMethodArgumentResolvers(methodArgumentResolvers);

    final List<MethodReturnValueHandler> methodReturnValueHandlers = new ArrayList<>();

    // SMART METERING
    methodReturnValueHandlers.add(this.smartMeteringManagementMarshallingPayloadMethodProcessor());
    methodReturnValueHandlers.add(this.smartMeteringBundleMarshallingPayloadMethodProcessor());
    methodReturnValueHandlers.add(this.smartMeteringCommonMarshallingPayloadMethodProcessor());
    methodReturnValueHandlers.add(
        this.smartMeteringInstallationMarshallingPayloadMethodProcessor());
    methodReturnValueHandlers.add(this.smartMeteringMonitoringMarshallingPayloadMethodProcessor());
    methodReturnValueHandlers.add(this.smartMeteringAdhocMarshallingPayloadMethodProcessor());
    methodReturnValueHandlers.add(
        this.smartMeteringConfigurationMarshallingPayloadMethodProcessor());

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
    LOGGER.debug("Creating Message Priority Interceptor Bean");

    return new SoapHeaderInterceptor(MESSAGE_PRIORITY_HEADER, MESSAGE_PRIORITY_HEADER);
  }

  @Bean
  public SoapHeaderInterceptor scheduleTimeInterceptor() {
    return new SoapHeaderInterceptor(MESSAGE_SCHEDULETIME_HEADER, MESSAGE_SCHEDULETIME_HEADER);
  }

  @Bean
  public SoapHeaderInterceptor maxScheduleTimeInterceptor() {
    return new SoapHeaderInterceptor(
        MESSAGE_MAXSCHEDULETIME_HEADER, MESSAGE_MAXSCHEDULETIME_HEADER);
  }

  @Bean
  public SoapHeaderInterceptor responseUrlInterceptor() {
    return new SoapHeaderInterceptor(MESSAGE_RESPONSE_URL_HEADER, MESSAGE_RESPONSE_URL_HEADER);
  }

  @Bean
  public SoapHeaderInterceptor bypassRetryInterceptor() {
    return new SoapHeaderInterceptor(BYPASS_RETRY_HEADER, BYPASS_RETRY_HEADER);
  }

  @Bean
  public EndpointInterceptor messageMetadataInterceptor() {
    return new SoapHeaderMessageMetadataInterceptor(MESSAGE_METADATA_CONTEXT_PROPERTY_NAME);
  }
}
