package com.ninetynine.codechallenge.provider.nasdaq

import java.time.Instant

class NasdaqProviderDTO {
    var code: String? = null
    var price: Double? = null
    var date: Long = Instant.now().toEpochMilli()

}