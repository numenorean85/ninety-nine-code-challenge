package com.ninetynine.codechallenge.company.service

import com.ninetynine.codechallenge.commons.round2
import com.ninetynine.codechallenge.commons.toLocalDateTimeUTC
import com.ninetynine.codechallenge.company.dto.CompanyDetailsCriteria
import com.ninetynine.codechallenge.company.dto.TimeSeriesType
import com.ninetynine.codechallenge.company.model.CompanySharePriceEntity
import com.ninetynine.codechallenge.config.properties.NinetyNineProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompanyServiceUtilsTest {

    @Test
    fun `computeTimeSeries() --- returns all data well computed`() {
        val sample = listOf(1.0,4.0,7.0,9.0,3.0,2.0)
        val result = computeTimeSeries("Hello", sample)
        with(result) {
            assertThat(timeSeries).isEqualTo("Hello")
            assertThat(min).isEqualTo(BigDecimal("1.00"))
            assertThat(max).isEqualTo(BigDecimal("9.00"))
            assertThat(avg).isEqualTo(BigDecimal("4.33"))
            assertThat(stdev).isEqualTo(BigDecimal("2.81"))
        }
    }

    @Test
    fun `getSharePricesInCurrency() --- When source currency is in USD and target currency is in EUR with exchange rate of 2,0 prices are doubled`() {
        val pricesUSD = listOf(1.0,4.0,7.0,9.0,3.0,2.0)
        val sharePrices = pricesUSD.map {
            CompanySharePriceEntity().apply {
                sharePrice = it.round2()
                currency = "USD"
            }
        }
        val pricesEUR = getSharePricesInCurrency(sharePrices, "EUR", "EUR") { _, _ -> 2.0 }
        pricesEUR.forEachIndexed { index, priceEUR ->
            assertThat(priceEUR).isEqualTo(pricesUSD[index] * 2.0)
        }
    }

    @Test
    fun `getSharePricesInCurrency() --- A null price is ignored and a null currency is replaced by the default`() {
        val sharePrices = (1..5).map {n ->
            CompanySharePriceEntity().apply {
                sharePrice = n.toBigDecimal().round2()
                currency = "USD"
                when (n) {
                    3 -> sharePrice = null
                    4 -> currency = null
                }
            }
        }

        val pricesEUR = getSharePricesInCurrency(sharePrices, "EUR", "JPY") { source, _ -> when (source) {
            "JPY" -> 2.0
            else -> 1.0
        }}
        assertThat(pricesEUR).hasSize(4)
        // Check that null currency record has taken a JPY, and doubled
        assertThat(pricesEUR[2]).isEqualTo(8.0)
    }

    @Test
    fun `applyDefaultsIfRequired() --- Check that all the defaults are right`() {
        val props = NinetyNineProperties().apply {
            defaultCurrency = "EUR"
            maxTimePeriods = 4
        }
        // No criteria defined, take all the defaults
        val criteria = CompanyDetailsCriteria()
        var targetCriteria = applyDefaultsIfRequired(criteria, props)
        var now = Instant.now().toLocalDateTimeUTC()
        with(targetCriteria) {
            assertThat(type).isEqualTo(TimeSeriesType.HOURLY)
            assertThat(periods).isEqualTo(4)
            assertThat(currencyCode).isEqualTo("EUR")
            assertThat(endTime).isEqualToIgnoringNanos(now)
            assertThat(startTime).isEqualToIgnoringNanos(now.minusHours(4))
        }
    }
}