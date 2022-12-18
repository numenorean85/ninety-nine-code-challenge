package com.ninetynine.codechallenge.provider.nyse

import java.time.Instant

class NyseProviderDTO {
    var companyCode: String? = null
    var value: Double? = null
    var time: Instant = Instant.now()
}