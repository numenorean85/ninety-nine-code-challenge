package com.ninetynine.codechallenge.commons

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.round2(): BigDecimal = this.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)

fun BigDecimal.round2(): BigDecimal = setScale(2, RoundingMode.HALF_EVEN)