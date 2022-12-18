package com.ninetynine.codechallenge.commons

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class DateAndNumberUtilsTest {

    private val localDateTime = LocalDateTime.of(2022, 12, 25, 14, 35, 29, 242)
    private val instant = localDateTime.toInstant(ZoneOffset.UTC)

    @Test
    fun `Instant_toLocalDateUTC() returns a local date from instant`() {
        assertThat(instant.toLocalDateUTC()).isEqualTo(LocalDate.of(2022, 12, 25))
    }

    @Test
    fun `Instant_toLocalDateTimeUTC() returns a local date time from instant`() {
        assertThat(instant.toLocalDateTimeUTC()).isEqualTo(LocalDateTime.of(2022, 12, 25, 14, 35, 29, 242))
    }

    @Test
    fun `Instant_toYearWeekUTC() returns a local date time from instant`() {
        val (year, week) = instant.toYearWeekUTC()
        assertThat(year).isEqualTo(2022)
        assertThat(week).isEqualTo(51)
    }

    @Test
    fun `Instant_toDateString() returns a local date from instant`() {
        assertThat(instant.toDateString()).isEqualTo("2022-12-25")
    }

    @Test
    fun `Instant_toDateTimeString() returns a local date time from instant`() {
        assertThat(instant.toDateHourString()).isEqualTo("2022-12-25T14:00")
    }

    @Test
    fun `Instant_toYearWeekString() returns a local date time from instant`() {
        assertThat(instant.toYearWeekString()).isEqualTo("2022-51")
    }

    @Test
    fun `LocalDateTime_toInstantUTC returns an Instant form LocalDateTime`() {
        assertThat(localDateTime.toInstantUTC()).isEqualTo(instant)
    }

    @Test
    fun `Double_round2() returns a BigDecimal rounded with 2 decimals`() {
        assertThat(12.346.round2()).isEqualTo(BigDecimal("12.35"))
    }

    @Test
    fun `BigDecimal_round2() returns a BigDecimal rounded with 2 decimals`() {
        assertThat(BigDecimal("12.346").round2()).isEqualTo(BigDecimal("12.35"))
    }

}