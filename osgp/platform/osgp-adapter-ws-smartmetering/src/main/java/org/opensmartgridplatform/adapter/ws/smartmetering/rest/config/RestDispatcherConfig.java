// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.smartmetering.rest.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Spring MVC configuration for the REST/JSON facade. This is the application context of the REST
 * {@code DispatcherServlet} that is registered additively next to the existing SOAP {@code
 * MessageDispatcherServlet}. Its parent context is the shared root application context, so the REST
 * controllers can inject the existing service and mapping beans via DI.
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "org.opensmartgridplatform.adapter.ws.smartmetering.rest")
public class RestDispatcherConfig {}
