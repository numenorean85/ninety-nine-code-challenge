package com.ninetynine.codechallenge.provider

import reactor.core.publisher.Mono

@FunctionalInterface
interface SharedPriceProvider {
    fun getSharePrices(): Mono<List<SharePriceDTO>>
}