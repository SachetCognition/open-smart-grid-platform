// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.shared.config;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.server.endpoint.adapter.method.MarshallingPayloadMethodProcessor;

/**
 * Factory with helpers to build the SOAP marshalling wiring that is shared by the {@code
 * osgp-adapter-ws-*} modules. Centralizes the repeated construction of {@link Jaxb2Marshaller} and
 * {@link MarshallingPayloadMethodProcessor} beans so each adapter only needs to supply the
 * module-specific JAXB context path.
 */
public final class Jaxb2MarshallerFactory {

  private Jaxb2MarshallerFactory() {
    // Utility class.
  }

  /**
   * Creates a {@link Jaxb2Marshaller} configured with the given JAXB context path.
   *
   * @param contextPath the JAXB context path for the marshaller
   * @return a configured Jaxb2Marshaller
   */
  public static Jaxb2Marshaller createMarshaller(final String contextPath) {
    final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    marshaller.setContextPath(contextPath);
    return marshaller;
  }

  /**
   * Creates a {@link MarshallingPayloadMethodProcessor} that uses the given marshaller both for
   * marshalling and unmarshalling.
   *
   * @param marshaller the marshaller to use as marshaller and unmarshaller
   * @return a MarshallingPayloadMethodProcessor backed by the given marshaller
   */
  public static MarshallingPayloadMethodProcessor createMethodProcessor(
      final Jaxb2Marshaller marshaller) {
    return new MarshallingPayloadMethodProcessor(marshaller, marshaller);
  }
}
