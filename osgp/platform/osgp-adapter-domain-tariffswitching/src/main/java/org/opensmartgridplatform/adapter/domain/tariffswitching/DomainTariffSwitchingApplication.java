// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.domain.tariffswitching;

import java.util.TimeZone;
import org.opensmartgridplatform.adapter.domain.tariffswitching.application.config.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot entry point for the tariff switching domain adapter.
 *
 * <p>WS6: This module was previously deployed as a WAR into an external Tomcat. It now runs as a
 * self-contained executable jar with an embedded servlet container. The existing hand-rolled {@link
 * ApplicationContext} configuration remains authoritative for the data source, JPA and JMS beans,
 * so the corresponding Spring Boot auto-configurations are excluded. Only the embedded web server
 * and Spring Boot Actuator (used for the liveness/readiness health probes) are contributed by
 * auto-configuration.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(
    exclude = {
      DataSourceAutoConfiguration.class,
      DataSourceTransactionManagerAutoConfiguration.class,
      HibernateJpaAutoConfiguration.class,
      ActiveMQAutoConfiguration.class,
      ArtemisAutoConfiguration.class,
      SecurityAutoConfiguration.class,
      ManagementWebSecurityAutoConfiguration.class
    })
@Import(ApplicationContext.class)
public class DomainTariffSwitchingApplication extends SpringBootServletInitializer {

  public static void main(final String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    SpringApplication.run(DomainTariffSwitchingApplication.class, args);
  }

  @Override
  protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    return builder.sources(DomainTariffSwitchingApplication.class);
  }
}
