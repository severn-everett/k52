package com.severett.k52.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal

class TransactionStatisticsTest : FunSpec({
    listOf(
        Pair(
            first = TransactionStatistics(
                sum = BigDecimal.valueOf(1234.5678),
                avg = BigDecimal.valueOf(43.455),
                max = BigDecimal.valueOf(1000),
                min = BigDecimal.valueOf(-155.251),
                count = 10L
            ),
            second = """{"sum":1234.5678,"avg":43.455,"min":-155.251,"max":1000.0,"count":10}""",
        ),
        Pair(
            first = TransactionStatistics(
                sum = BigDecimal.ZERO,
                avg = BigDecimal.ZERO,
                max = BigDecimal.ZERO,
                min = BigDecimal.ZERO,
                count = 0L
            ),
            second = """{"sum":0.0,"avg":0.0,"min":0.0,"max":0.0,"count":0}""",
        ),
    ).forEach { (statistics, expectedOutput) ->
        test("Serializing statistics[$statistics] should provide correct output") {
            val serializedStats = Json.encodeToString(statistics)
            serializedStats shouldBe expectedOutput
        }
    }
})
