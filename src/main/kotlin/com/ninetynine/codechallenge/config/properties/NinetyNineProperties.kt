package com.ninetynine.codechallenge.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpMethod

@ConfigurationProperties(prefix = "ninety-nine")
class NinetyNineProperties {
    var maxTimePeriods: Int = 10
    var defaultCurrency: String = "EUR"
    var provider: Map<String, ProviderProperties> = mapOf()
    var providerRate: String = "P20S"
}

class ProviderProperties {
    lateinit var url: String
    var method: HttpMethod = HttpMethod.GET
}