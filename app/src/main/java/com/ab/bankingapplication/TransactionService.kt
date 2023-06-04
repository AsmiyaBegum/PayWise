package com.ab.bankingapplication

import com.ab.bankingapplication.api.RetrofitWrapper
import com.ab.bankingapplication.model.CurrenciesModel

class TransactionService  {

    private val transactionService = RetrofitWrapper.transactionService


    suspend fun getCurrency(): Result<List<CurrenciesModel>> {
        return  kotlin.runCatching {
            transactionService.getCurrencies()
        }
    }
}

