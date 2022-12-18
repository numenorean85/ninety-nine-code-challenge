package com.ninetynine.codechallenge.mock

import com.ninetynine.codechallenge.company.persistance.CompanyRepository
import com.ninetynine.codechallenge.provider.nasdaq.NasdaqProviderDTO
import com.ninetynine.codechallenge.provider.nyse.NyseProviderDTO
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import kotlin.random.Random
import kotlin.random.asJavaRandom

@Profile("local")
@RestController
@RequestMapping("/mock")
class ProviderMockController(
    private val companyRepository: CompanyRepository
) {

    private val prices: MutableMap<String, Double>
    private val random = Random.Default.asJavaRandom()

    init {
        prices = companyRepository.findAll()
            .asSequence()
            .mapNotNull { it.code }
            .map { it to random.nextDouble(50.0, 100.0) }
            .toMap()
            .toMutableMap()
    }

    @GetMapping("/nasdaq")
    fun getNasdaqSharePrices(): List<NasdaqProviderDTO> {
        return listOf("AAPL", "MSFT").map { code ->
            NasdaqProviderDTO().apply {
                this.code = code
                val referencePrice = prices[code] ?: 75.0
                price = random.nextGaussian(referencePrice, 0.2 * referencePrice)
                date = Instant.now().toEpochMilli()
            }
        }
    }

    @GetMapping("/nyse")
    fun getNYSESharePrices(): List<NyseProviderDTO> {
        return listOf(NyseProviderDTO().apply {
            this.companyCode = "ORCL"
            val referencePrice = prices["ORCL"] ?: 75.0
            value = random.nextGaussian(referencePrice, 0.2 * referencePrice)
            time = Instant.now()
        })
    }
}