// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.ws.smartmetering.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Enables springdoc-openapi for this (non Spring Boot) web application. The springdoc
 * auto-configuration classes are imported explicitly because there is no Spring Boot
 * auto-configuration in this WAR. This serves {@code /rest/v3/api-docs} (OpenAPI JSON) and the
 * Swagger UI at {@code /rest/swagger-ui/index.html} from the REST dispatcher servlet.
 */
@Configuration
@Import({
  SpringDocConfiguration.class,
  SpringDocConfigProperties.class,
  SwaggerUiConfigProperties.class,
  SwaggerUiOAuthProperties.class,
  SpringDocWebMvcConfiguration.class,
  SwaggerConfig.class
})
public class OpenApiConfig {

  @Bean
  public OpenAPI smartMeteringOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("GXF Smart Metering REST API")
                .description(
                    "REST/JSON facade for the smart metering web service adapter, exposed "
                        + "alongside the existing SOAP endpoints.")
                .version("v1")
                .license(new License().name("Apache-2.0")));
  }
}
