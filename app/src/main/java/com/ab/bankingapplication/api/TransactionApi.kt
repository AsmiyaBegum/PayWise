package com.ab.bankingapplication.api

import com.ab.bankingapplication.model.CurrenciesModel
import retrofit2.http.GET

interface TransactionApi {

    @GET("ListOfCurrenciesByName/JSON/debug")
    suspend fun getCurrencies(): List<CurrenciesModel>
}