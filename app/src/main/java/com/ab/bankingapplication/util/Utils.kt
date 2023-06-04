package com.ab.bankingapplication.util

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.ab.bankingapplication.BankingApplication
import com.ab.bankingapplication.BuildConfig
import com.ab.bankingapplication.R
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

object Utils {

    fun sharedPreferences(): SharedPreferences {
        return BankingApplication.appContext()!!.getSharedPreferences("banking_app_shared_pref", Context.MODE_PRIVATE)
    }
    fun setSharedPrefStr(key: String, value: String) = sharedPreferences().edit().putString(key, value).apply()


    fun secureSharedPreferences(): SharedPreferences {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            sharedPreferences()
        } else {
            BankingApplication.securedSharedPref()
        }
    }

    fun setSecureSharedPreferences(key: String, value: String) = secureSharedPreferences().edit().putString(key, value).apply()


    fun View.showVisibility(condition: Boolean) {
        this.visibility = if (condition) View.VISIBLE else View.GONE
    }

    fun isBuildVariantDebug(): Boolean = BuildConfig.BUILD_TYPE == Constants.BUILD_TYPE_DEBUG


    fun setAppState(state : String) = setSecureSharedPreferences(Constants.SHARED_PREF_APP_STATE, state)

    private fun isValidIndianMobileNumber(number: String): Boolean {
        val regex = Regex("^[6-9]\\d{9}$")
        return regex.matches(number)
    }

    fun isIndianMobileNumber(number: String): Boolean = Patterns.PHONE.matcher(number).matches() && isValidIndianMobileNumber(number)

    fun getUserMobileNumber() = secureSharedPreferences().getString(Constants.SHARED_PREF_USER_MOBILE_NUMBER,"").toString()

    fun snackBarListener(
        view: View,
        text: String,
        snack: (Snackbar) -> Unit,
        okayButtonEvent: (Unit) -> Unit = { }
    ) {
        val snackBar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.key_okay) { action -> okayButtonEvent(Unit) }
        val textView = snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setAllCaps(true)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.maxLines = 10
        snackBar.show()
        snack(snackBar)
    }

    fun generateRandomOTP(): String {
        val digits = mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        val otpBuilder = StringBuilder()

        repeat(4) {
            val randomIndex = Random.nextInt(digits.size)
            val randomDigit = digits[randomIndex]
            otpBuilder.append(randomDigit)
            digits.removeAt(randomIndex)
        }

        return otpBuilder.toString()
    }

    fun  Double.getAmountWithCurrency(): String{
        return this.toString()
    }

    private fun allowLogging(): Boolean {
        return BuildConfig.DEBUG
    }

    fun logMessage(tag: String, msg: String, mode: String) {
        if (allowLogging()) {
            when (mode) {
                Constants.DEBUG_MODE -> {
                    Log.d(tag, msg)
                }
                Constants.ERROR_MODE -> {
                    Log.e(tag, msg)
                }
                Constants.INFO_MODE -> {
                    Log.i(tag, msg)
                }
            }
        }
    }



    fun View?.openKeyboard(showKeyboard: Boolean, context: Context?) {
        val inputMethodManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (showKeyboard) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        } else {
            inputMethodManager.hideSoftInputFromWindow(this?.windowToken, 0)
        }
    }

    fun isAppInForeground(context: Context?): Boolean {
        val am = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = am.getRunningTasks(1)
        if (!tasks.isEmpty()) {
            val topActivity = tasks[0].topActivity
            if (topActivity?.packageName != context?.packageName) {
                return false
            }
        }
        return true
    }


    internal fun checkInternetConnection(): Boolean {
        val connectivityManager = BankingApplication.appContext()
            ?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isConnected = connectivityManager.activeNetworkInfo?.isConnected
        return isConnected ?: false
    }

    fun TextView.maskCardNumber(cardNumber : String,mask : Boolean,length : Int = cardNumber.length){
        if(mask){
            val stringBuilder = StringBuilder()
            cardNumber.forEachIndexed { index, c ->
                if(index < length  && c != ' '){
                    stringBuilder.append('*')
                } else {
                    stringBuilder.append(cardNumber[index])
                }
            }
            this.text = stringBuilder.toString()
        }else{
            this.text = cardNumber
        }

    }

    fun TextView.formatDate(date : Date,dateFormat : String){
        val dateFormatter = SimpleDateFormat("MM/dd", Locale.US)
        val formattedDate: String = dateFormatter.format(date)
        this.text = formattedDate
    }

    fun TextView.formatAmount(formatAmount : String,smallAfterDecimal : Boolean){
        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance()

        // Format the amount with commas using NumberFormat
        val formattedAmount: String = currencyFormat.format(formatAmount)
        // Create a SpannableString with the formatted amount
        val spannableString = SpannableString(formattedAmount)

        if(smallAfterDecimal){
            // Find the index of the decimal point
            val decimalIndex = formattedAmount.indexOf(".")

            // Apply smaller text size to the ".05" part
            spannableString.setSpan(RelativeSizeSpan(0.7f), decimalIndex, formattedAmount.length, 0)
        }
        this.text = spannableString
    }


}