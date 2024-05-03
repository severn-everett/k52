package com.severett.k52.plugins

import com.severett.k52.model.AddTransactionResult
import com.severett.k52.model.Transaction
import com.severett.k52.services.TransactionService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }
    val transactionService by inject<TransactionService>()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/statistics") {
            call.respond(transactionService.getStatistics())
        }

        post<Transaction>("/transactions") { transaction ->
            val httpStatusCode = when (transactionService.addTransaction(transaction)) {
                AddTransactionResult.SUCCESS -> HttpStatusCode.Created
                AddTransactionResult.TRANSACTION_EXPIRED -> HttpStatusCode.BadRequest
            }
            call.response.status(httpStatusCode)
        }

        delete("/transactions") {
            transactionService.deleteTransactions()
            call.response.status(HttpStatusCode.NoContent)
        }
    }
}
