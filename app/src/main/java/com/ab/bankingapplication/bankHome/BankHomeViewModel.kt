package com.ab.bankingapplication.bankHome

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ab.bankingapplication.service.TransactionService
import com.ab.bankingapplication.model.CurrenciesModel
import kotlinx.coroutines.*

class BankHomeViewModel : ViewModel() {

    private val transactionRepository = TransactionService()
    private val networkScope = CoroutineScope(Dispatchers.IO)

    private val _transactionList = MutableLiveData<List<CurrenciesModel>>()
    val transaction: LiveData<List<CurrenciesModel>>
        get() = _transactionList

    fun fetchTransaction() {

        networkScope.launch {
            try {
                val resultUser = transactionRepository.getCurrency()
                withContext(Dispatchers.Main) {
                    resultUser.onSuccess {
                        _transactionList.value = it
                        Log.d("resposne_trascat",it.toString())
                    }
                    resultUser.onFailure {
                        it
                    }
                }
            } catch (e: Exception) {
                // Handle the error scenario
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkScope.cancel() // Cancel the coroutine scope when the ViewModel is cleared
    }
}