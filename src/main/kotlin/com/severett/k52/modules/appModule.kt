package com.severett.k52.modules

import com.severett.k52.services.TransactionService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::TransactionService)
}
