// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.shared.application.config.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/** Verifies the compiled-in {@code @Value} defaults of {@link DefaultJmsConfiguration}. */
class DefaultJmsConfigurationTest {

  private DefaultJmsConfiguration resolveDefaults() {
    try (final AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext()) {
      context.register(PropertySourcesPlaceholderConfigurer.class);
      context.register(DefaultJmsConfiguration.class);
      context.refresh();
      return context.getBean(DefaultJmsConfiguration.class);
    }
  }

  @Test
  void brokerTypeDefaultsToArtemis() {
    assertThat(this.resolveDefaults().getBrokerType()).isEqualTo(JmsBrokerType.ARTEMIS);
  }

  @Test
  void trustAllPackagesDefaultsToFalse() {
    assertThat(this.resolveDefaults().isTrustAllPackages()).isFalse();
  }

  @Test
  void trustedPackagesListIsAuthoritative() {
    assertThat(this.resolveDefaults().getTrustedPackages())
        .isEqualTo("org.opensmartgridplatform,org.joda.time,java.util");
  }

  @Test
  void brokerUrlDefaultsToArtemisCompatibleCoreUrl() {
    final String brokerUrl = this.resolveDefaults().getBrokerUrl();
    // The Artemis core client does not understand the ActiveMQ Classic "failover:(...)" syntax.
    assertThat(brokerUrl).doesNotContain("failover:");
    assertThat(brokerUrl).isEqualTo("tcp://localhost:61616");
  }
}
