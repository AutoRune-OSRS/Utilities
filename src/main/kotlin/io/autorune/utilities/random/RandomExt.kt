package io.autorune.utilities.random

import java.security.SecureRandom

fun SecureRandom.nextDouble(low: Double, high: Double) : Double {
    val rnd = nextDouble()
    return rnd * high + (1.0 - rnd) * low + rnd;
}