// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.tariffswitching;

import java.util.TimeZone;
import org.opensmartgridplatform.adapter.ws.tariffswitching.application.config.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.jdbc.DataSourcePoolMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.webservices.WebServicesAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

/**
 * Spring Boot entry point for the tariff switching web-service (SOAP) adapter.
 *
 * <p>WS6: This module was previously deployed as a WAR into an external Tomcat, with the Spring-WS
 * {@link MessageDispatcherServlet} registered by a {@code WebApplicationInitializer}. It now runs
 * as a self-contained, executable Spring Boot WAR with an embedded servlet container. The SOAP
 * servlet is registered here as a {@link ServletRegistrationBean} so it works identically whether
 * the artifact is launched with {@code java -jar} or deployed to an external container. The
 * existing hand-rolled {@link ApplicationContext} configuration remains authoritative for the data
 * source, JPA and JMS beans, so the corresponding auto-configurations are excluded. Actuator runs
 * on a separate management port (see application.yml) because the SOAP servlet is mapped to {@code
 * /*}.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(
    exclude = {
      DataSourceAutoConfiguration.class,
      DataSourceTransactionManagerAutoConfiguration.class,
      HibernateJpaAutoConfiguration.class,
      FlywayAutoConfiguration.class,
      ActiveMQAutoConfiguration.class,
      ArtemisAutoConfiguration.class,
      SecurityAutoConfiguration.class,
      ManagementWebSecurityAutoConfiguration.class,
      PropertyPlaceholderAutoConfiguration.class,
      DataSourcePoolMetricsAutoConfiguration.class,
      WebServicesAutoConfiguration.class
    })
@Import(ApplicationContext.class)
public class TariffSwitchingWsApplication extends SpringBootServletInitializer {

  public static void main(final String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    SpringApplication.run(TariffSwitchingWsApplication.class, args);
  }

  /**
   * Lenient placeholder resolver matching the original Tomcat behaviour: unresolved placeholders
   * are left untouched instead of failing application startup.
   */
  @Bean
  static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    final PropertySourcesPlaceholderConfigurer configurer =
        new PropertySourcesPlaceholderConfigurer();
    configurer.setIgnoreUnresolvablePlaceholders(true);
    return configurer;
  }

  @Override
  protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    return builder.sources(TariffSwitchingWsApplication.class);
  }

  /**
   * Registers the Spring-WS SOAP dispatcher servlet on the embedded servlet container. The servlet
   * is given its own {@link AnnotationConfigWebApplicationContext} whose parent is the Spring Boot
   * root context. Because the servlet creates and refreshes that child context itself, its {@code
   * initStrategies()} runs and detects the endpoint mappings, adapters and interceptors declared by
   * {@code <sws:annotation-driven/>} in the (ancestor) root context. Passing the already-refreshed
   * root context directly skips {@code initStrategies()}, leaving the dispatcher without endpoint
   * mappings so every SOAP request fails with a 404.
   */
  @Bean
  public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
      final org.springframework.context.ApplicationContext rootContext) {
    final AnnotationConfigWebApplicationContext servletContext =
        new AnnotationConfigWebApplicationContext();
    servletContext.setParent(rootContext);

    final MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(servletContext);
    servlet.setTransformWsdlLocations(true);
    final ServletRegistrationBean<MessageDispatcherServlet> registration =
        new ServletRegistrationBean<>(servlet, "/*");
    registration.setName("spring-ws");
    registration.setLoadOnStartup(1);
    return registration;
  }
}
