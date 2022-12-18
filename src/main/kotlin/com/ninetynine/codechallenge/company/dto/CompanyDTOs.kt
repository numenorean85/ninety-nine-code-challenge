package com.ninetynine.codechallenge.company.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.util.Currency

open class CompanyLightDTO {
    var id: Long? = null
    var name: String? = null
    var code: String? = null
    var description: String? = null
    var latestSharePrice: SharePriceDTO? = null
}

class CompanyDTO : CompanyLightDTO() {
    var sharePriceTimeSeries: List<TimeSeriesDTO> = listOf()
    var timeSeriesType: TimeSeriesType? = null
    var currency: Currency? = null
}

class CompanyDetailsCriteria {
    var type: TimeSeriesType? = null
    var periods: Int? = null
    var startTime: LocalDateTime? = null
    var endTime: LocalDateTime? = null
    var currencyCode: String? = null

    // Field shortcuts to be called as a request parameters
    var t by ::type
    var p by ::periods
    var st by ::startTime
    var et by ::endTime
    var c by ::currencyCode
}

enum class TimeSeriesType {
    HOURLY, DAILY, WEEKLY;
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SharePriceDTO(
    var price: BigDecimal,
    var currency: Currency? = null,
    var updatedAt: Instant? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class TimeSeriesDTO {
    lateinit var timeSeries: String
    lateinit var min: BigDecimal
    lateinit var max: BigDecimal
    lateinit var avg: BigDecimal
    lateinit var stdev: BigDecimal
}

