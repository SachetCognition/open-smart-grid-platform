// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.shared.config;

import org.opensmartgridplatform.adapter.ws.endpointinterceptors.CertificateAndSoapHeaderAuthorizationEndpointInterceptor;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.SoapHeaderEndpointInterceptor;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.WebServiceMonitorInterceptor;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.WebServiceMonitorInterceptorCapabilities;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.X509CertificateRdnAttributeValueEndpointInterceptor;
import org.opensmartgridplatform.shared.application.config.AbstractConfig;
import org.springframework.context.annotation.Bean;

/**
 * Base configuration shared by the {@code osgp-adapter-ws-*} web service configurations. Provides
 * the SOAP endpoint interceptor beans and the header/context constants that are wired identically
 * in every adapter, so each concrete {@code WebServiceConfig} only has to declare its
 * module-specific marshallers, method processors and endpoint mappings.
 *
 * <p>Bean names are the method names and are kept identical to the previous per-module definitions,
 * because they are referenced by name from each module's {@code applicationContext.xml}.
 */
public abstract class AbstractWebServiceConfig extends AbstractConfig {

  protected static final String ORGANISATION_IDENTIFICATION_HEADER = "OrganisationIdentification";
  protected static final String ORGANISATION_IDENTIFICATION_CONTEXT =
      ORGANISATION_IDENTIFICATION_HEADER;

  protected static final String MESSAGE_PRIORITY_HEADER = "MessagePriority";
  protected static final String USER_NAME_HEADER = "UserName";
  protected static final String APPLICATION_NAME_HEADER = "ApplicationName";

  protected static final String X509_RDN_ATTRIBUTE_ID = "cn";
  protected static final String X509_RDN_ATTRIBUTE_VALUE_CONTEXT_PROPERTY_NAME = "CommonNameSet";

  protected static final String SERVER = "SERVER";

  private static final String PROPERTY_NAME_SOAP_MESSAGE_LOGGING_ENABLED =
      "soap.message.logging.enabled";
  private static final String PROPERTY_NAME_SOAP_MESSAGE_PRINTING_ENABLED =
      "soap.message.printing.enabled";

  @Bean
  public X509CertificateRdnAttributeValueEndpointInterceptor
      x509CertificateSubjectCnEndpointInterceptor() {
    return new X509CertificateRdnAttributeValueEndpointInterceptor(
        X509_RDN_ATTRIBUTE_ID, X509_RDN_ATTRIBUTE_VALUE_CONTEXT_PROPERTY_NAME);
  }

  @Bean
  public SoapHeaderEndpointInterceptor organisationIdentificationInterceptor() {
    return new SoapHeaderEndpointInterceptor(
        ORGANISATION_IDENTIFICATION_HEADER, ORGANISATION_IDENTIFICATION_CONTEXT);
  }

  @Bean
  public CertificateAndSoapHeaderAuthorizationEndpointInterceptor
      organisationIdentificationInCertificateCnEndpointInterceptor() {
    return new CertificateAndSoapHeaderAuthorizationEndpointInterceptor(
        X509_RDN_ATTRIBUTE_VALUE_CONTEXT_PROPERTY_NAME, ORGANISATION_IDENTIFICATION_CONTEXT);
  }

  @Bean
  public WebServiceMonitorInterceptor webServiceMonitorInterceptor() {
    final boolean soapMessageLoggingEnabled =
        this.environment.getProperty(
            PROPERTY_NAME_SOAP_MESSAGE_LOGGING_ENABLED, boolean.class, false);
    final boolean soapMessagePrintingEnabled =
        this.environment.getProperty(
            PROPERTY_NAME_SOAP_MESSAGE_PRINTING_ENABLED, boolean.class, true);

    final WebServiceMonitorInterceptorCapabilities capabilities =
        new WebServiceMonitorInterceptorCapabilities(
            soapMessageLoggingEnabled, soapMessagePrintingEnabled);

    return new WebServiceMonitorInterceptor(
        ORGANISATION_IDENTIFICATION_HEADER,
        USER_NAME_HEADER,
        APPLICATION_NAME_HEADER,
        capabilities);
  }
}
