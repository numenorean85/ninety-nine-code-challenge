package com.ninetynine.codechallenge.company.service

import com.ninetynine.codechallenge.commons.*
import com.ninetynine.codechallenge.company.dto.*
import com.ninetynine.codechallenge.company.persistance.CompanyRepository
import com.ninetynine.codechallenge.company.persistance.CompanySharePriceRepository
import com.ninetynine.codechallenge.company.model.CompanyEntity
import com.ninetynine.codechallenge.company.model.CompanySharePriceEntity
import com.ninetynine.codechallenge.config.properties.NinetyNineProperties
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

@Service
@Transactional
class CompanyService(
    private val companyRepository: CompanyRepository,
    private val companySharePriceRepository: CompanySharePriceRepository,
    private val props: NinetyNineProperties,
) {
    fun getAllCompanies(pageable: Pageable): Page<CompanyLightDTO> {
        return companyRepository.findAll(pageable).map { c ->
            mapCompanyToDTO(c, CompanyLightDTO())
        }
    }

    fun getCompanyDetailsByCode(
        code: String,
        criteria: CompanyDetailsCriteria
    ): CompanyDTO {

        val definitiveCriteria = applyDefaultsIfRequired(criteria, props)

        val company = validateParametersAndGetCompany(code, definitiveCriteria)

        val list = companySharePriceRepository.getSharePricesByCompanyAndPeriod(
            companyId = company.id!!,
            startInstant = definitiveCriteria.startTime?.toInstantUTC()!!,
            endInstant = definitiveCriteria.endTime?.toInstantUTC()!!
        )

        return mapCompanyToDTO(company, CompanyDTO()).apply {
        val type = definitiveCriteria.type!!
            timeSeriesType = type
            sharePriceTimeSeries = list
                // Group the result by time depending on time series type: hourly, daily or weekly
                // The result is a map of time periods + list of prices within that time period (each one containing a price for a given timestamp)
                .groupBy { item ->
                    when(type) {
                        TimeSeriesType.HOURLY -> item.createdAt.toDateHourString()
                        TimeSeriesType.DAILY -> item.createdAt.toDateString()
                        TimeSeriesType.WEEKLY -> item.createdAt.toYearWeekString()
                    }
                }
                // With the results group, compute statistics as min, max, average and standard deviation
                .map { (time, sharePrices) ->
                    val values = getSharePricesInCurrency(sharePrices, definitiveCriteria.currencyCode!!, props.defaultCurrency, ::getExchangeRate)
                    computeTimeSeries(time, values)
                }
            currency = Currency.getInstance(definitiveCriteria.currencyCode) }
        }

    private fun validateParametersAndGetCompany (
        companyCode: String,
        criteria: CompanyDetailsCriteria
    ): CompanyEntity {

        // As this method is called after applyDefaultsIfRequired, we should only check that startDate <= endDate
        if(criteria.startTime!! > criteria.endTime!!) {
            throw Exception("Start time (st) should be before or equal to End time (et)")
        }

        return companyRepository.findByCode(companyCode)
            ?: throw Exception("Company with code $companyCode not found")
    }

    private fun getExchangeRate(sourceCurrency: String, targetCurrency: String): Double {
        // TODO call to forex service to obtain current exchange rate
        return 1.0
    }
}

