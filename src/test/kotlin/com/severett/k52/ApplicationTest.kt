package com.severett.k52

import com.severett.k52.modules.appModule
import com.severett.k52.plugins.configureRouting
import com.severett.k52.services.TransactionService
import com.severett.k52.util.toDateTimeString
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.get
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.koin.test.KoinTest
import org.koin.test.inject
import java.time.Instant
import java.time.temporal.ChronoUnit

private const val EMPTY_RESPONSE = """{"sum":0.0,"avg":0.0,"min":0.0,"max":0.0,"count":0}"""

class ApplicationTest : KoinTest, FunSpec() {
    private val transactionService by inject<TransactionService>()

    init {
        test("Valid transactions should be added and calculated") {
            runTest {
                val baseTimestamp = Instant.now()
                val transactionsList = buildList {
                    val timestampOne = baseTimestamp.minusSeconds(5).toDateTimeString()
                    add(10.0 to timestampOne)
                    add(15.0 to timestampOne)
                    add(-5.0 to baseTimestamp.minusSeconds(11).toDateTimeString())
                    val timestampTwo = baseTimestamp.minusSeconds(25).toDateTimeString()
                    add(20000.256525 to timestampTwo)
                    add(2525.10101 to timestampTwo)
                }
                transactionsList.forEach { (amount, timestamp) ->
                    client.post("/transactions") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"amount":$amount,"timestamp":"$timestamp"}""")
                    }.apply { status shouldBe HttpStatusCode.Created }
                }
                client.get("/statistics") {
                    accept(ContentType.Application.Json)
                }.bodyAsText() shouldBe """{"sum":22545.36,"avg":4509.08,"min":-5.0,"max":20000.26,"count":5}"""
            }
        }

        test("Expired transactions should not be calculated") {
            runTest {
                val validTimestamp = Instant.now().minusSeconds(55).toDateTimeString()
                client.post("/transactions") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"amount":12345.678,"timestamp":"$validTimestamp"}""")
                }
                delay(8 * 1000)
                checkNoStatistics()
            }
        }

        test("Expired transactions should not be accepted") {
            runTest {
                val expiredTimestamp = Instant.now().minus(5, ChronoUnit.DAYS).toDateTimeString()
                client.post("/transactions") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"amount":12345.678,"timestamp":"$expiredTimestamp"}""")
                }.apply {
                    assertSoftly {
                        status shouldBe HttpStatusCode.BadRequest
                        checkNoStatistics()
                    }
                }
            }
        }

        test("Transactions should be removable") {
            runTest {
                val timestamp = Instant.now().minusSeconds(5).toDateTimeString()
                repeat(5) {
                    client.post("/transactions") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"amount":12345.678,"timestamp":"$timestamp"}""")
                    }.apply {
                        status shouldBe HttpStatusCode.Created
                    }
                }
                client.delete("/transactions").apply {
                    assertSoftly {
                        status shouldBe HttpStatusCode.NoContent
                        checkNoStatistics()
                    }
                }
            }
        }

        test("An invalid JSON structure should be rejected") {
            runTest {
                val response = client.post("/transactions") {
                    contentType(ContentType.Application.Json)
                    setBody("INVALID_JSON_STRUCTURE")
                }
                assertSoftly {
                    response.status shouldBe HttpStatusCode.BadRequest
                    checkNoStatistics()
                }
            }
        }

        genInvalidTransactionData().forEach { (amountStr, timestampStr) ->
            val paramsList = buildList {
                if (amountStr.isNotEmpty()) {
                    add("\"amount\":$amountStr")
                }
                if (timestampStr.isNotEmpty()) {
                    add("\"timestamp\":$timestampStr")
                }
            }
            val requestContent = "{${paramsList.joinToString(",")}}"
            test("Invalid request content of [$requestContent] will be rejected") {
                runTest {
                    val response = client.post("/transactions") {
                        contentType(ContentType.Application.Json)
                        setBody(requestContent)
                    }
                    assertSoftly {
                        response.status shouldBe HttpStatusCode.BadRequest
                        checkNoStatistics()
                    }
                }
            }
        }
    }
}

private fun genInvalidTransactionData(): List<Pair<String, String>> {
    val validAmount = "12345.678'"
    val validTimestamp = Instant.now().minusSeconds(5).toDateTimeString()
    val tomorrowTimestamp = Instant.now()
        .plus(1, ChronoUnit.DAYS)
        .toDateTimeString()
    return listOf(
        validAmount to "",
        validAmount to "null",
        validAmount to "5",
        validAmount to "\"01/01/2021\"",
        validAmount to "\"$tomorrowTimestamp\"",
        "" to validTimestamp,
        "null" to validTimestamp,
        "\"NON_NUMBER\"" to validTimestamp,
    )
}

private inline fun runTest(crossinline testBlock: suspend ApplicationTestBuilder.() -> Unit) {
    testApplication {
        application {
            install(Koin) {
                slf4jLogger()
                modules(appModule)
            }

            configureRouting()
        }

        try {
            testBlock.invoke(this)
        } finally {
            val transactionService = get<TransactionService>(TransactionService::class.java)
            transactionService.deleteTransactions()
        }
    }
}

private suspend fun ApplicationTestBuilder.checkNoStatistics() {
    val response = client.get("/statistics") {
        accept(ContentType.Application.Json)
    }
    response.bodyAsText() shouldBe EMPTY_RESPONSE
}
