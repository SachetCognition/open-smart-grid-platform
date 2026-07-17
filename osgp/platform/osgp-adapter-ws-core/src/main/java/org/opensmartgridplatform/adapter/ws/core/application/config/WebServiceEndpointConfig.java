// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.core.application.config;

import org.opensmartgridplatform.adapter.ws.endpointinterceptors.CertificateAndSoapHeaderAuthorizationEndpointInterceptor;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.SoapHeaderEndpointInterceptor;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.SoapHeaderInterceptor;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.WebServiceMonitorInterceptor;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.X509CertificateRdnAttributeValueEndpointInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;

/**
 * Java configuration replacing the former {@code applicationContext.xml}. It registers the
 * annotation driven {@link PayloadRootAnnotationMethodEndpointMapping} together with the endpoint
 * interceptors (equivalent to {@code <sws:annotation-driven/>} plus {@code <sws:interceptors>} in
 * the same order).
 *
 * <p>Note: {@code @EnableWs} is intentionally not used here. It would register an additional default
 * {@code defaultMethodEndpointAdapter} without the OSGP JAXB {@code MarshallingPayloadMethodProcessor}
 * beans, which can shadow the adapter defined in {@link WebServiceConfig} and cause "No adapter for
 * endpoint" errors. Declaring the endpoint mapping explicitly keeps {@link WebServiceConfig}'s
 * adapter authoritative, exactly as the original XML did.
 */
@Configuration
public class WebServiceEndpointConfig {

  private final X509CertificateRdnAttributeValueEndpointInterceptor
      x509CertificateSubjectCnEndpointInterceptor;
  private final SoapHeaderEndpointInterceptor organisationIdentificationInterceptor;
  private final SoapHeaderInterceptor messagePriorityInterceptor;
  private final CertificateAndSoapHeaderAuthorizationEndpointInterceptor
      organisationIdentificationInCertificateCnEndpointInterceptor;
  private final PayloadValidatingInterceptor payloadValidatingInterceptor;
  private final WebServiceMonitorInterceptor webServiceMonitorInterceptor;

  public WebServiceEndpointConfig(
      @Qualifier("x509CertificateSubjectCnEndpointInterceptor")
          final X509CertificateRdnAttributeValueEndpointInterceptor
              x509CertificateSubjectCnEndpointInterceptor,
      @Qualifier("organisationIdentificationInterceptor")
          final SoapHeaderEndpointInterceptor organisationIdentificationInterceptor,
      @Qualifier("messagePriorityInterceptor")
          final SoapHeaderInterceptor messagePriorityInterceptor,
      @Qualifier("organisationIdentificationInCertificateCnEndpointInterceptor")
          final CertificateAndSoapHeaderAuthorizationEndpointInterceptor
              organisationIdentificationInCertificateCnEndpointInterceptor,
      @Qualifier("payloadValidatingInterceptor")
          final PayloadValidatingInterceptor payloadValidatingInterceptor,
      @Qualifier("webServiceMonitorInterceptor")
          final WebServiceMonitorInterceptor webServiceMonitorInterceptor) {
    this.x509CertificateSubjectCnEndpointInterceptor = x509CertificateSubjectCnEndpointInterceptor;
    this.organisationIdentificationInterceptor = organisationIdentificationInterceptor;
    this.messagePriorityInterceptor = messagePriorityInterceptor;
    this.organisationIdentificationInCertificateCnEndpointInterceptor =
        organisationIdentificationInCertificateCnEndpointInterceptor;
    this.payloadValidatingInterceptor = payloadValidatingInterceptor;
    this.webServiceMonitorInterceptor = webServiceMonitorInterceptor;
  }

  @Bean
  public PayloadRootAnnotationMethodEndpointMapping payloadRootAnnotationMethodEndpointMapping() {
    final PayloadRootAnnotationMethodEndpointMapping mapping =
        new PayloadRootAnnotationMethodEndpointMapping();
    mapping.setInterceptors(
        new EndpointInterceptor[] {
          this.x509CertificateSubjectCnEndpointInterceptor,
          this.organisationIdentificationInterceptor,
          this.messagePriorityInterceptor,
          this.organisationIdentificationInCertificateCnEndpointInterceptor,
          this.payloadValidatingInterceptor,
          this.webServiceMonitorInterceptor
        });
    mapping.setOrder(0);
    return mapping;
  }
}
