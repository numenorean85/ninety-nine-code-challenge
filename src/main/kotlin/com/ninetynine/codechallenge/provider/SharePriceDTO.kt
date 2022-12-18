package com.ninetynine.codechallenge.provider

import java.math.BigDecimal
import java.time.Instant

class SharePriceDTO {
    var company: String? = null
    var sharePrice: BigDecimal? = null
    var time: Instant? = null
    var currency: String? = null
}