package com.ninetynine.codechallenge.provider

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninetynine.codechallenge.config.properties.NinetyNineProperties
import com.ninetynine.codechallenge.config.properties.ProviderProperties
import com.ninetynine.codechallenge.provider.nasdaq.NasdaqProvider
import com.ninetynine.codechallenge.provider.nasdaq.NasdaqProviderDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.Instant

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NasdaqProviderTest {

    private val mapper = ObjectMapper()

    private val sharePrices = listOf("AAPL", "MSFT").map { code ->
        NasdaqProviderDTO().apply {
            this.code = code
            price = 75.0
            date = Instant.now().toEpochMilli()
        }
    }

    private val props = NinetyNineProperties().apply {
        provider = mapOf("nasdaq" to ProviderProperties().apply {
            method = HttpMethod.GET
            url = "http://localhost:8080/fake"
        })
    }
    
    private val webClient: WebClient = WebClient.builder()
        .exchangeFunction {
            Mono.just(ClientResponse
                .create(HttpStatus.OK)
                .headers {
                    it.contentType = MediaType.APPLICATION_JSON
                    it.accept = listOf(MediaType.APPLICATION_JSON)
                }
                .body(mapper.writeValueAsString(sharePrices))
                .build()
            )
        }
        .build()

    private lateinit var provider: NasdaqProvider

    @BeforeAll
    fun init() {
        provider = NasdaqProvider(props, webClient)
    }

    @Test
    fun `getSharePrices() --- Check that call returns a list of 2 items`() {
        provider.getSharePrices().subscribe { list ->
            assertThat(list).hasSize(2).allSatisfy {
                assertThat(it.sharePrice).isEqualTo(BigDecimal("75.00"))
                assertThat(it.company).isIn("AAPL", "MSFT")
                assertThat(it.currency).isNull()
            }
        }
    }
}