package com.ninetynine.codechallenge.company.service

import com.ninetynine.codechallenge.commons.round2
import com.ninetynine.codechallenge.commons.toDateHourString
import com.ninetynine.codechallenge.commons.toDateString
import com.ninetynine.codechallenge.commons.toInstantUTC
import com.ninetynine.codechallenge.company.dto.CompanyDetailsCriteria
import com.ninetynine.codechallenge.company.dto.TimeSeriesType
import com.ninetynine.codechallenge.company.model.CompanyEntity
import com.ninetynine.codechallenge.company.model.CompanyLatestSharePriceEntity
import com.ninetynine.codechallenge.company.model.CompanySharePriceEntity
import com.ninetynine.codechallenge.company.persistance.CompanyRepository
import com.ninetynine.codechallenge.company.persistance.CompanySharePriceRepository
import com.ninetynine.codechallenge.config.properties.NinetyNineProperties
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompanyServiceTest {
    private val companyRepository: CompanyRepository = mock()

    private val sharePriceRepository: CompanySharePriceRepository = mock()

    private lateinit var service: CompanyService

    private val companies = listOf("AAPL" to "Apple", "MSFT" to "Microsoft").mapIndexed { index, (code, name) ->
        CompanyEntity().apply {
            id = index.toLong()
            this.code = code
            this.name = name
            latestSharePrice = CompanyLatestSharePriceEntity().apply {
                id = index.toLong()
                sharePrice = BigDecimal("20.0")
                currency = "EUR"
                lastUpdate = Instant.now()
            }
        }
    }

    private val referenceDate = LocalDateTime.of(2022, 12, 15, 10, 10)
    private val sharePrices = mutableListOf<CompanySharePriceEntity>()

    @BeforeAll
    fun init() {
        service = CompanyService(
            companyRepository = companyRepository,
            companySharePriceRepository = sharePriceRepository,
            props = NinetyNineProperties()
        )

        whenever(companyRepository.findAll(any<Pageable>())).thenAnswer {
            val pageable = it.getArgument<Pageable>(0)
            pageable.run {
                PageImpl(companies.subList(offset.toInt(), (pageable.offset + pageable.pageSize).toInt()), pageable, companies.size.toLong())
            }

        }

        whenever(companyRepository.findByCode(any())).thenAnswer {
            val code = it.getArgument<String>(0)
            companies.firstOrNull { c -> c.code == code }
        }

        val apple = companies.first { it.code == "AAPL" }
        for (week in 0L..2L) {
            for (day in 0L..3L) {
                for (hour in 0L..2L) {
                    var price = 10.0
                    for (minute in 0L..2L) {
                        val date = referenceDate
                            .minusWeeks(0)
                            .minusDays(day)
                            .minusHours(hour)
                            .minusMinutes(minute)
                        val instant = date.toInstantUTC()
                        sharePrices.add(CompanySharePriceEntity().apply {
                            id = instant.toEpochMilli()
                            company = apple
                            sharePrice = price.round2()
                            currency = "EUR"
                            createdAt = instant
                        })
                        price += 10.0

                    }
                }
            }
        }

        whenever(sharePriceRepository.getSharePricesByCompanyAndPeriod(any(), any(), any())).thenAnswer { inv ->
            val companyId = inv.getArgument<Long>(0)
            val range = inv.getArgument<Instant>(1)..inv.getArgument(2)
            sharePrices.filter { sp -> sp.company.id == companyId && sp.createdAt in range }
        }
    }

    @Test
    fun `Get All --- Returns a list of 2 companies`() {
        val result = service.getAllCompanies(PageRequest.of(0, 2))
        assertThat(result.content)
            .hasSize(2)
            .extracting("code")
            .contains("AAPL", "MSFT")
    }

    @Test
    fun `Get share prices --- when criteria is HOURLY, returns 3 time series for 2022-12-14, 10_00, 09_00 and 08_00`() {
        val criteria = CompanyDetailsCriteria().apply {
            type = TimeSeriesType.HOURLY
            endTime = referenceDate
        }
        val result = service.getCompanyDetailsByCode("AAPL", criteria)
        // As we have 3 lines by hour and 3 hours by day, time series should be 3
        with(result) {
            assertThat(code).isEqualTo("AAPL")
            assertThat(currency?.currencyCode).isEqualTo("EUR")
            assertThat(timeSeriesType).isEqualTo(TimeSeriesType.HOURLY)
            assertThat(sharePriceTimeSeries).hasSize(3)
            assertThat(sharePriceTimeSeries).allSatisfy { ts ->
                assertThat(ts.timeSeries).isIn((0L..2L).map {
                    referenceDate.minusHours(it)
                        .toInstantUTC()
                        .toDateHourString()
                })
                assertThat(ts.min).isEqualTo(BigDecimal("10.00"))
                assertThat(ts.max).isEqualTo(BigDecimal("30.00"))
                assertThat(ts.avg).isEqualTo(BigDecimal("20.00"))
            }
        }

    }

    @Test
    fun `Get share prices --- when criteria is DAILY, returns 4 time series for 2022-12-15, 14, 13 and 12`() {
        val criteria = CompanyDetailsCriteria().apply {
            type = TimeSeriesType.DAILY
            endTime = referenceDate
        }
        val result = service.getCompanyDetailsByCode("AAPL", criteria)
        // As we have 3 lines by hour and 3 hours by day, time series should be 3
        with(result) {
            assertThat(code).isEqualTo("AAPL")
            assertThat(currency?.currencyCode).isEqualTo("EUR")
            assertThat(timeSeriesType).isEqualTo(TimeSeriesType.DAILY)
            assertThat(sharePriceTimeSeries).hasSize(4)
            assertThat(sharePriceTimeSeries).allSatisfy { ts ->
                assertThat(ts.timeSeries).isIn((0L..3L).map {
                    referenceDate.minusDays(it)
                        .toInstantUTC()
                        .toDateString()
                })
                assertThat(ts.min).isEqualTo(BigDecimal("10.00"))
                assertThat(ts.max).isEqualTo(BigDecimal("30.00"))
                assertThat(ts.avg).isEqualTo(BigDecimal("20.00"))
            }
        }
    }

    @Test
    fun `Get share prices --- when company is not found, an exception is thrown`() {
        assertThrows<Exception> {
            service.getCompanyDetailsByCode("ORCL", CompanyDetailsCriteria())
        }
    }

    @Test
    fun `Get share prices --- when start time is after end time, an exception is thrown`() {
        assertThrows<Exception> {
            service.getCompanyDetailsByCode("ORCL", CompanyDetailsCriteria().apply {
                startTime = LocalDateTime.now()
                endTime = referenceDate
            })
        }
    }
}