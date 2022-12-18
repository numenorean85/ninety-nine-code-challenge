package com.ninetynine.codechallenge.mock

import com.ninetynine.codechallenge.commons.round2
import com.ninetynine.codechallenge.commons.toLocalDateTimeUTC
import com.ninetynine.codechallenge.company.model.CompanyLatestSharePriceEntity
import com.ninetynine.codechallenge.company.model.CompanySharePriceEntity
import com.ninetynine.codechallenge.company.persistance.CompanyRepository
import com.ninetynine.codechallenge.company.persistance.CompanySharePriceRepository
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import kotlin.random.Random
import kotlin.random.asJavaRandom

@Profile("local")
@RestController
@RequestMapping("api/gen")
@Transactional
class DataGeneratorController(
    private val companyRepository: CompanyRepository,
    private val companySharePriceRepository: CompanySharePriceRepository,
) {

    @PostMapping("/share-price")
    fun generateSharePricesForAllCompanies() {

        var currentInstant = Instant.now()
        val random = Random.Default.asJavaRandom()

        val targetInstant = currentInstant.minusSeconds(24L * 3600L * 30)
        val companyMap = companyRepository.findAll().associateWith { company ->
            val price = random.nextDouble(50.0, 100.0)
            company.latestSharePrice = (company.latestSharePrice ?: CompanyLatestSharePriceEntity()).apply {
                sharePrice = price.round2()
                currency = "EUR"
                lastUpdate = currentInstant
                this.company = company
            }
            companyRepository.save(company)
            price
        }.toMutableMap()

        val f = File("src/main/resources/db/local/migration", "V1_0_2__initial_share_price_values.sql")
        val pw = PrintWriter(FileWriter(f, false))

        pw.use {
            it.println("insert into company_share_price(company_id, share_price, currency, created_at) values ")
            while(currentInstant > targetInstant) {
                val zdt = currentInstant.toLocalDateTimeUTC()
                currentInstant = currentInstant.minusSeconds(30)
                companyMap.forEach { (company, price) ->
                    val newPrice = random.nextGaussian(price, price * 0.2)
                    it.println("(${company.id}, ${DecimalFormat("0.0#").format(newPrice)}, 'EUR', '${DateTimeFormatter.ISO_DATE_TIME.format(zdt)}'),")
                }

            }
            // Print the latest line to avoid the script finishing by a comma,
            it.println("(1, 0.0, 'EUR', '1990-01-01T00:00:00.000000');")
        }
    }
}