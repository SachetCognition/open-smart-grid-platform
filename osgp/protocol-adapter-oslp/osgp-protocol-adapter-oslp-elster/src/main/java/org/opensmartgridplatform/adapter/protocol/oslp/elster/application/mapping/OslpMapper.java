// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.oslp.elster.application.mapping;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

@Component
public class OslpMapper extends ConfigurableMapper {

  private static final String TIME_FORMAT = "yyyyMMddHHmmss";

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT);

  @Override
  protected void configure(final MapperFactory factory) {
    factory.getConverterFactory().registerConverter(new IntegerToByteStringConverter());
    factory
        .getConverterFactory()
        .registerConverter(new ConfigurationToOslpSetConfigurationRequestConverter());
    factory
        .getConverterFactory()
        .registerConverter(new OslpGetConfigurationResponseToConfigurationConverter());
    factory.getConverterFactory().registerConverter(new RelayMatrixConverter());
    factory
        .getConverterFactory()
        .registerConverter(new DaliConfigurationToOslpDaliConfigurationConverter());
    factory
        .getConverterFactory()
        .registerConverter(new RelayConfigurationToOslpRelayConfigurationConverter());
    factory.getConverterFactory().registerConverter(new LightTypeConverter());
    factory.getConverterFactory().registerConverter(new LinkTypeConverter());
    factory.getConverterFactory().registerConverter(new RelayTypeConverter());
    factory.getConverterFactory().registerConverter(new RelayDataConverter());

    // Converter from String to ZonedDateTime using the OSLP time format.
    factory
        .getConverterFactory()
        .registerConverter(
            new CustomConverter<String, ZonedDateTime>() {

              @Override
              public ZonedDateTime convert(
                  final String source,
                  final Type<? extends ZonedDateTime> destinationType,
                  final MappingContext context) {
                return LocalDateTime.parse(source, TIME_FORMATTER).atZone(ZoneOffset.UTC);
              }
            });

    // Converter from ZonedDateTime to String using the OSLP time format.
    factory
        .getConverterFactory()
        .registerConverter(
            new CustomConverter<ZonedDateTime, String>() {

              @Override
              public String convert(
                  final ZonedDateTime source,
                  final Type<? extends String> destinationType,
                  final MappingContext context) {
                return TIME_FORMATTER.format(source.withZoneSameInstant(ZoneOffset.UTC));
              }
            });
  }
}
