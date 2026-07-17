// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.smartmetering.application.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.opensmartgridplatform.adapter.ws.smartmetering.rest.config.RestDispatcherConfig;
import org.opensmartgridplatform.shared.application.config.AbstractWsAdapterInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/** Web application Java configuration class. */
public class AdapterInitializer extends AbstractWsAdapterInitializer {

  private static final String REST_DISPATCHER_SERVLET_NAME = "rest-dispatcher";
  private static final String REST_DISPATCHER_SERVLET_MAPPING = "/rest/*";

  public AdapterInitializer() {
    super(ApplicationContext.class, "java:comp/env/osgp/AdapterWsSmartMetering/log-config");
  }

  /**
   * Registers the existing SOAP {@code MessageDispatcherServlet} (via the superclass) and,
   * additively, a Spring MVC {@code DispatcherServlet} for the REST/JSON facade mounted under
   * {@code /rest}. The SOAP servlet mapping is left untouched.
   */
  @Override
  public void onStartup(final ServletContext servletContext) throws ServletException {
    super.onStartup(servletContext);

    final AnnotationConfigWebApplicationContext restContext =
        new AnnotationConfigWebApplicationContext();
    restContext.register(RestDispatcherConfig.class);

    final DispatcherServlet restDispatcherServlet = new DispatcherServlet(restContext);

    final ServletRegistration.Dynamic restDispatcher =
        servletContext.addServlet(REST_DISPATCHER_SERVLET_NAME, restDispatcherServlet);
    restDispatcher.setLoadOnStartup(2);
    restDispatcher.addMapping(REST_DISPATCHER_SERVLET_MAPPING);
  }
}
