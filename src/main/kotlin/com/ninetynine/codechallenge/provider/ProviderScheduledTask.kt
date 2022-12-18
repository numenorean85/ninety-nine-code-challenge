package com.ninetynine.codechallenge.provider

import com.ninetynine.codechallenge.commons.round2
import com.ninetynine.codechallenge.company.model.CompanyEntity
import com.ninetynine.codechallenge.company.model.CompanyLatestSharePriceEntity
import com.ninetynine.codechallenge.company.model.CompanySharePriceEntity
import com.ninetynine.codechallenge.company.persistance.CompanyRepository
import com.ninetynine.codechallenge.company.persistance.CompanySharePriceRepository
import org.slf4j.Logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import java.time.Instant

@Component
@Transactional
class ProviderScheduledTask(
    private val logger: Logger,
    private val providers: List<SharedPriceProvider>,
    private val companyRepository: CompanyRepository,
    private val companySharePriceRepository: CompanySharePriceRepository
) {

    @Scheduled(fixedRateString = "\${ninety-nine.provider-rate}")
    fun scheduleSharePrice() {
        /*
            1. For each provider, obtain share prices (a provider may return values from different companies)
            2. Flatten provider results in a single list of results
            3. For each company with a new share price:
                3.1. Update the latest price entity (CompanyLatestSharePriceEntity)
                3.2. Add a new instance of CompanySharePriceEntity
         */
        val companies = this.companies
        Flux.fromIterable(providers)
            .flatMap { it.getSharePrices() }
            .flatMapIterable { it }
            .subscribe { sharePrice ->
                sharePrice.company?.let { code ->
                    companies[code]?.let { company ->
                        // 1st. Update the company with the latest price
                        val savedCompany = updateLatestPrice(company, sharePrice)
                        // 2nd. Add a new row in share price table
                        createSharePriceEntry(savedCompany, sharePrice)
                        logger.info("$code - ${sharePrice.sharePrice?.round2()} ${sharePrice.currency} (${sharePrice.time})")
                    }
                }
            }
    }

    private fun updateLatestPrice (company: CompanyEntity, sharePrice: SharePriceDTO): CompanyEntity {
        company.latestSharePrice = (company.latestSharePrice ?: CompanyLatestSharePriceEntity()).apply {
            this.sharePrice = sharePrice.sharePrice
            this.company = company
            currency = sharePrice.currency
            lastUpdate = sharePrice.time ?: Instant.now()
        }
        return companyRepository.save(company)
    }

    private fun createSharePriceEntry(company: CompanyEntity, sharePrice: SharePriceDTO): CompanySharePriceEntity {
        val sharePriceEntity = CompanySharePriceEntity().apply {
            this.company = company
            this.sharePrice = sharePrice.sharePrice
            currency = sharePrice.currency
            createdAt = sharePrice.time ?: Instant.now()
        }
        return companySharePriceRepository.save(sharePriceEntity)
    }

    private val companies get() = companyRepository.findAll()
        .associateBy { it.code!! }
}