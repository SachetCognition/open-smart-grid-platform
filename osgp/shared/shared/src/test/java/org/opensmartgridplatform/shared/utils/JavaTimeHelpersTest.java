package org.opensmartgridplatform.shared.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;
import org.junit.jupiter.api.Test;

class JavaTimeHelpersTest {

  public static final int MILLIS_TO_SECONDS = 1000;

  @Test
  void shouldCropNanoToMillis() {
    final ZonedDateTime dateWithNano =
        ZonedDateTime.of(1998, 1, 24, 1, 1, 1, 123999999, ZoneId.systemDefault());
    final long croppedMillis = JavaTimeHelpers.getMillisFrom(dateWithNano);
    assertThat(croppedMillis).isEqualTo(123);
  }

  @Test
  void shouldFormatDate() {
    final Instant instant = Instant.ofEpochMilli(1000L);
    final Date date = Date.from(instant);

    final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
    final String expected =
        ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    final String formattedJava = JavaTimeHelpers.formatDate(date, formatter);

    assertThat(formattedJava).isEqualTo(expected);
  }

  @Test
  void shouldMapGregorianCalendar() {
    final ZonedDateTime zonedDateTime =
        ZonedDateTime.of(1998, 1, 24, 1, 1, 1, 123999999, ZoneId.systemDefault());
    final GregorianCalendar gregorianCalendar = GregorianCalendar.from(zonedDateTime);

    final ZonedDateTime expected =
        ZonedDateTime.ofInstant(gregorianCalendar.toInstant(), ZoneId.of("UTC"));
    final ZonedDateTime javaApi =
        JavaTimeHelpers.gregorianCalendarToZonedDateTime(gregorianCalendar, ZoneId.of("UTC"));

    this.validateDates(javaApi, expected);
  }

  @Test
  void shouldDetermineDayLightSavingsCorrectly() {
    final ZonedDateTime daylightSavingDate =
        ZonedDateTime.of(2023, 3, 26, 2, 0, 0, 0, ZoneId.of("Europe/Amsterdam"));

    final ZonedDateTime notDaylightSavingDate =
        ZonedDateTime.of(2023, 3, 25, 2, 0, 0, 0, ZoneId.of("Europe/Amsterdam"));

    assertThat(JavaTimeHelpers.isDayLightSavingsActive(daylightSavingDate)).isTrue();
    assertThat(JavaTimeHelpers.isDayLightSavingsActive(notDaylightSavingDate)).isFalse();
  }

  @Test
  void shouldReturnOffsetInMillis() {
    final Instant instant = Instant.ofEpochMilli(1000L);

    final ZonedDateTime java = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

    final int expectedOffset =
        ZoneId.systemDefault().getRules().getOffset(instant).getTotalSeconds() * MILLIS_TO_SECONDS;
    final int javaOffset = JavaTimeHelpers.getOffsetForZonedDateTimeInMillis(java);

    assertThat(javaOffset).isEqualTo(expectedOffset);
  }

  @Test
  void shouldParseDates() {
    final String localDateString = "1998-01-24";
    final String localDateTimeString = "1998-01-24T13:00:00";
    final String zonedDateTimeString = "2015-03-29T02:00:00.000+01:00";

    final ZonedDateTime localDateParsedJava = JavaTimeHelpers.parseToZonedDateTime(localDateString);
    final ZonedDateTime localDateTimeParsedJava =
        JavaTimeHelpers.parseToZonedDateTime(localDateTimeString);
    final ZonedDateTime zonedDateTimeParsedJava =
        JavaTimeHelpers.parseToZonedDateTime(zonedDateTimeString);

    final ZonedDateTime localDateExpected =
        LocalDate.of(1998, 1, 24).atStartOfDay(ZoneId.systemDefault());
    final ZonedDateTime localDateTimeExpected =
        LocalDateTime.of(1998, 1, 24, 13, 0, 0).atZone(ZoneId.systemDefault());
    final ZonedDateTime zonedDateTimeExpected =
        ZonedDateTime.of(2015, 3, 29, 2, 0, 0, 0, ZoneOffset.ofHours(1));

    this.validateDates(localDateParsedJava, localDateExpected);
    this.validateDates(localDateTimeParsedJava, localDateTimeExpected);
    this.validateDates(zonedDateTimeParsedJava, zonedDateTimeExpected);
  }

  private void validateDates(final ZonedDateTime javaApi, final ZonedDateTime expected) {
    assertThat(javaApi.getYear()).isEqualTo(expected.getYear());
    assertThat(javaApi.getMonthValue()).isEqualTo(expected.getMonthValue());
    assertThat(javaApi.getDayOfMonth()).isEqualTo(expected.getDayOfMonth());
    assertThat(javaApi.getHour()).isEqualTo(expected.getHour());
    assertThat(javaApi.getMinute()).isEqualTo(expected.getMinute());
    assertThat(javaApi.getSecond()).isEqualTo(expected.getSecond());
    assertThat(javaApi.toInstant()).isEqualTo(expected.toInstant());
  }
}
