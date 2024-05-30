package com.severett.k52

import com.severett.k52.plugins.configureRouting
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*

class ApplicationTest : FunSpec({
    test("Test Application") {
        testApplication {
            application {
                configureRouting()
            }
            client.get("/").apply {
                status shouldBe HttpStatusCode.OK
                bodyAsText() shouldBe "Hello World!"
            }
        }
    }
})
