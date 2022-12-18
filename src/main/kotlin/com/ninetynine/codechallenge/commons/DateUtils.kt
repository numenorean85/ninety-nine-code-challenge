package com.ninetynine.codechallenge.commons

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields

fun Instant.toLocalDateUTC(): LocalDate = LocalDate.ofInstant(this, ZoneOffset.UTC)

fun Instant.toLocalDateTimeUTC(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneOffset.UTC)

fun Instant.toYearWeekUTC(): Pair<Int, Int> {
    val localDate = toLocalDateUTC()
    val weekNumber: Int = localDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    val weekYear: Int = localDate.get(IsoFields.WEEK_BASED_YEAR)
    return weekYear to weekNumber
}

fun Instant.toDateString(): String = toLocalDateUTC().toString()

fun Instant.toDateHourString(): String = toLocalDateTimeUTC().run {
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00")
        .format(this)
}

fun Instant.toYearWeekString(): String = toYearWeekUTC().let { (year, week) -> "$year-$week" }

fun LocalDateTime.toInstantUTC(): Instant = toInstant(ZoneOffset.UTC)