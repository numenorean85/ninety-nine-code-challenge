package com.ninetynine.codechallenge.provider.nasdaq

import com.ninetynine.codechallenge.config.properties.NinetyNineProperties
import com.ninetynine.codechallenge.provider.SharePriceDTO
import com.ninetynine.codechallenge.provider.SharedPriceProvider
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.Instant

@Component
class NasdaqProvider(
    props: NinetyNineProperties,
    private val webClient: WebClient
) : SharedPriceProvider {

    private val providerProperties = props.provider[PROVIDER_NAME]!!

    override fun getSharePrices(): Mono<List<SharePriceDTO>> {
        return webClient
            .method(providerProperties.method)
            .uri(providerProperties.url)
            .retrieve()
            .bodyToMono<List<NasdaqProviderDTO>>()
            .map { inputList ->
                inputList.map(this::mapObjects)
            }
    }

    private fun mapObjects(inputDTO: NasdaqProviderDTO): SharePriceDTO {
        return SharePriceDTO().apply {
            company = inputDTO.code
            time = Instant.ofEpochMilli(inputDTO.date)
            sharePrice = inputDTO.price?.let { BigDecimal(it) }
            currency = "EUR"
        }
    }

    companion object {
        private const val PROVIDER_NAME = "nasdaq"
    }

}