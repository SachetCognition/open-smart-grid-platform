// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.core.application.config;

import java.util.List;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.CertificateAndSoapHeaderAuthorizationEndpointInterceptor;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.SoapHeaderEndpointInterceptor;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.SoapHeaderInterceptor;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.WebServiceMonitorInterceptor;
import org.opensmartgridplatform.adapter.ws.endpointinterceptors.X509CertificateRdnAttributeValueEndpointInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;

/**
 * Java configuration replacing the former {@code applicationContext.xml}. It enables annotation
 * driven Spring Web Services support (equivalent to {@code <sws:annotation-driven/>}) and registers
 * the endpoint interceptors (equivalent to {@code <sws:interceptors>}) in the same order.
 */
@EnableWs
@Configuration
public class WebServiceEndpointConfig extends WsConfigurerAdapter {

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

  @Override
  public void addInterceptors(final List<EndpointInterceptor> interceptors) {
    interceptors.add(this.x509CertificateSubjectCnEndpointInterceptor);
    interceptors.add(this.organisationIdentificationInterceptor);
    interceptors.add(this.messagePriorityInterceptor);
    interceptors.add(this.organisationIdentificationInCertificateCnEndpointInterceptor);
    interceptors.add(this.payloadValidatingInterceptor);
    interceptors.add(this.webServiceMonitorInterceptor);
  }
}
