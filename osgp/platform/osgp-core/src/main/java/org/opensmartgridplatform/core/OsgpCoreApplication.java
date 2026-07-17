// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.core;

import java.util.TimeZone;
import org.opensmartgridplatform.core.application.config.ApplicationContext;
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
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Spring Boot entry point for osgp-core.
 *
 * <p>WS6: This module was previously deployed as a WAR into an external Tomcat. It now runs as a
 * self-contained executable jar with an embedded servlet container. The existing hand-rolled {@link
 * ApplicationContext} configuration remains authoritative for the data source, JPA, JMS and Flyway
 * beans, so the corresponding Spring Boot auto-configurations are excluded to avoid conflicting
 * bean definitions. Only the embedded web server and Spring Boot Actuator (used for the
 * liveness/readiness health probes) are contributed by auto-configuration.
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
      DataSourcePoolMetricsAutoConfiguration.class
    })
@Import(ApplicationContext.class)
public class OsgpCoreApplication extends SpringBootServletInitializer {

  public static void main(final String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    SpringApplication.run(OsgpCoreApplication.class, args);
  }

  /**
   * Lenient placeholder resolver matching the original Tomcat behaviour: unresolved placeholders
   * (e.g. the deprecated {@code hibernate.ejb.naming_strategy}) are left untouched instead of
   * failing application startup.
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
    return builder.sources(OsgpCoreApplication.class);
  }
}
