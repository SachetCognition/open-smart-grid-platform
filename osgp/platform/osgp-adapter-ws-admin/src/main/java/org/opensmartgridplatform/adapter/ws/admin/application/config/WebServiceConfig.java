// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.admin.application.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.opensmartgridplatform.adapter.ws.admin.application.exceptionhandling.DetailSoapFaultMappingExceptionResolver;
import org.opensmartgridplatform.adapter.ws.admin.application.exceptionhandling.SoapFaultMapper;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.AnnotationMethodArgumentResolver;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.OrganisationIdentification;
import org.opensmartgridplatform.adapter.ws.shared.config.AbstractWebServiceConfig;
import org.opensmartgridplatform.adapter.ws.shared.config.Jaxb2MarshallerFactory;
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
@PropertySource("classpath:osgp-adapter-ws-admin.properties")
@PropertySource(value = "file:${osgp/Global/config}", ignoreResourceNotFound = true)
@PropertySource(value = "file:${osgp/AdapterWsAdmin/config}", ignoreResourceNotFound = true)
public class WebServiceConfig extends AbstractWebServiceConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebServiceConfig.class);

  private static final String PROPERTY_NAME_MARSHALLER_CONTEXT_PATH_DEVICE_MANAGEMENT =
      "jaxb2.marshaller.context.path.devicemanagement";

  /** Method for creating the Marshaller for device management. */
  @Bean
  public Jaxb2Marshaller deviceManagementMarshaller() {
    LOGGER.debug("Creating Device Management Marshaller Bean");

    return Jaxb2MarshallerFactory.createMarshaller(
        this.environment.getRequiredProperty(
            PROPERTY_NAME_MARSHALLER_CONTEXT_PATH_DEVICE_MANAGEMENT));
  }

  /** Method for creating the Marshalling Payload Method Processor for device management. */
  @Bean
  public MarshallingPayloadMethodProcessor deviceManagementMarshallingPayloadMethodProcessor() {
    LOGGER.debug("Creating Device Management Marshalling Payload Method Processor Bean");

    return Jaxb2MarshallerFactory.createMethodProcessor(this.deviceManagementMarshaller());
  }

  /** Method for creating the Default Method Endpoint Adapter. */
  @Bean
  public DefaultMethodEndpointAdapter defaultMethodEndpointAdapter() {
    LOGGER.debug("Creating Default Method Endpoint Adapter Bean");

    final DefaultMethodEndpointAdapter defaultMethodEndpointAdapter =
        new DefaultMethodEndpointAdapter();

    final List<MethodArgumentResolver> methodArgumentResolvers = new ArrayList<>();

    methodArgumentResolvers.add(this.deviceManagementMarshallingPayloadMethodProcessor());

    methodArgumentResolvers.add(
        new AnnotationMethodArgumentResolver(
            ORGANISATION_IDENTIFICATION_CONTEXT, OrganisationIdentification.class));
    defaultMethodEndpointAdapter.setMethodArgumentResolvers(methodArgumentResolvers);

    final List<MethodReturnValueHandler> methodReturnValueHandlers = new ArrayList<>();

    methodReturnValueHandlers.add(this.deviceManagementMarshallingPayloadMethodProcessor());

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
}
