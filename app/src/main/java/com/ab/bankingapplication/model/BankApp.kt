package com.ab.bankingapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date


data class CardDetails(
    var cardId : Int,
    var cardName : String,
    var cardBalance : Double,
    var creditCardNumber : String,
    var creditCardExpiredDate : Date,
    var bgColor : Int
)

@Parcelize
data class TransactionDetails(
    var senderName : String = "",
    var receiverName : String = "",
    var senderMobileNumber : String = "",
    var receiverMobileNumber : String = "",
    var amount : Double = 0.0,
    var transactionNumber : String = "",
    var transactionFee : Double = 0.0,
    var transactionDate : Date= Date()
) : Parcelable


data class CurrenciesModel(
    var sISOCode : String,
    var sName : String
)