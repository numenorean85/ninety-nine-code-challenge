package com.ninetynine.codechallenge.provider

import com.ninetynine.codechallenge.company.model.CompanyEntity
import com.ninetynine.codechallenge.company.model.CompanyLatestSharePriceEntity
import com.ninetynine.codechallenge.company.model.CompanySharePriceEntity
import com.ninetynine.codechallenge.company.persistance.CompanyRepository
import com.ninetynine.codechallenge.company.persistance.CompanySharePriceRepository
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.slf4j.Logger
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.Instant

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProviderScheduledTaskTest {

    private val logger: Logger = mock()
    private val companyRepository: CompanyRepository = mock()
    private val sharePriceRepository: CompanySharePriceRepository = mock()

    private lateinit var providerScheduledTask: ProviderScheduledTask

    private val companyMap = listOf("AAPL" to "Apple", "MSFT" to "Microsoft").mapIndexed { index, (code, name) ->
        index.toLong() to CompanyEntity().apply {
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
    }.toMap().toMutableMap()

    class TestSharedPriceProvider(
        vararg codes: String
    ) : SharedPriceProvider {

        private val codes = codes.toList()

        override fun getSharePrices(): Mono<List<SharePriceDTO>> {
            return Mono.just(codes.map { code ->
                SharePriceDTO().apply {
                    company = code
                    sharePrice = BigDecimal.TEN
                    time = Instant.now()
                    currency = "EUR"
                }
            })
        }

    }

    @BeforeAll
    fun init() {
        providerScheduledTask = ProviderScheduledTask(
            logger = logger,
            providers = listOf(
                TestSharedPriceProvider("MSFT", "ORCL"),
                TestSharedPriceProvider("AAPL")
            ),
            companyRepository = companyRepository,
            companySharePriceRepository = sharePriceRepository
        )

        whenever(companyRepository.findAll()).thenReturn(companyMap.values.toList())
        whenever(companyRepository.save(any<CompanyEntity>())).thenAnswer {
            it.getArgument<CompanyEntity>(0)
        }
        whenever(sharePriceRepository.save(any<CompanySharePriceEntity>())).thenAnswer {
            it.getArgument<CompanySharePriceRepository>(0)
        }
    }

    @Test
    fun `scheduleSharePrice() --- Check that repositories have been called properly`() {
        providerScheduledTask.scheduleSharePrice()
        verify(companyRepository, times(1)).findAll()
        verify(companyRepository, times(2)).save(any())
        verify(sharePriceRepository, times(2)).save(any())
    }
}