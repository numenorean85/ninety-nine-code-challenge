package com.ninetynine.codechallenge.company.service

import com.ninetynine.codechallenge.commons.round2
import com.ninetynine.codechallenge.commons.toLocalDateTimeUTC
import com.ninetynine.codechallenge.company.dto.*
import com.ninetynine.codechallenge.company.model.CompanyEntity
import com.ninetynine.codechallenge.company.model.CompanySharePriceEntity
import com.ninetynine.codechallenge.config.properties.NinetyNineProperties
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

internal fun computeTimeSeries(
    timeSeries: String,
    sharePrices: List<Double>,
): TimeSeriesDTO {
    return TimeSeriesDTO().apply {
        this.timeSeries = timeSeries
        min = sharePrices.min().round2()
        max = sharePrices.max().round2()
        val valuesSize = sharePrices.size
        val mean = sharePrices.sumOf { it } / valuesSize
        avg = mean.round2()
        stdev = sqrt(sharePrices.sumOf { (it - mean).pow(2.0) } / valuesSize).round2()
    }
}

internal fun getSharePricesInCurrency(
    sharePrices: List<CompanySharePriceEntity>,
    targetCurrency: String,
    defaultCurrency: String,
    exchangeRateFunction: (String, String) -> Double
): List<Double> {
    // We use this map to avoid re-compute exchange rates every time
    val currencyExchangeRates: MutableMap<String, Double> = mutableMapOf()
    return sharePrices
        .asSequence()
        .mapNotNull {
            val sharePrice = it.sharePrice?.toDouble()
            val sourceCurrency = it.currency ?: defaultCurrency
            when {
                sharePrice == null -> null
                targetCurrency != sourceCurrency -> {
                    // If share price is in another currency than the target, we should obtain the exchange rate (TODO)
                    currencyExchangeRates.computeIfAbsent(sourceCurrency) { code ->
                        exchangeRateFunction(code, targetCurrency)
                    }
                    val exchangeRate = currencyExchangeRates.getOrDefault(sourceCurrency, 1.0)
                    sharePrice * exchangeRate
                }
                else -> sharePrice
            }
        }
        .toList()
}

internal fun applyDefaultsIfRequired(
    inputCriteria: CompanyDetailsCriteria,
    props: NinetyNineProperties
): CompanyDetailsCriteria {
    return CompanyDetailsCriteria().apply {
        val t = inputCriteria.type ?: TimeSeriesType.HOURLY
        // If no period is specified, the system default is taken
        val p = inputCriteria.periods ?: props.maxTimePeriods
        // If no end time is specified, we will take the current time value
        val et = inputCriteria.endTime ?: Instant.now().toLocalDateTimeUTC()
        type = t
        periods = p
        endTime = et
        // Start time is the one passed as a parameter OR the subtraction of p periods, depending on times series type
        startTime = inputCriteria.startTime ?: when (t) {
            TimeSeriesType.HOURLY -> et.minusHours(p.toLong())
            TimeSeriesType.DAILY -> et.minusDays(p.toLong())
            TimeSeriesType.WEEKLY -> et.minusWeeks(p.toLong())
        }
        currencyCode = inputCriteria.currencyCode ?: props.defaultCurrency
    }
}


internal fun <T : CompanyLightDTO> mapCompanyToDTO(
    company: CompanyEntity,
    dto: T
): T {
    return dto.apply {
        id = company.id
        name = company.name
        description = company.description
        code = company.code
        latestSharePrice = company.latestSharePrice?.let { sp ->
            SharePriceDTO(
                price = sp.sharePrice ?: BigDecimal.ZERO,
                updatedAt = sp.lastUpdate,
                currency = Currency.getInstance(sp.currency)
            )
        }
    }
}