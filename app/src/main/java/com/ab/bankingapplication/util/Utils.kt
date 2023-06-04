package com.ab.bankingapplication.util

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ab.bankingapplication.BankingApplication
import com.ab.bankingapplication.BuildConfig
import android.provider.Settings
import android.text.style.ForegroundColorSpan
import com.ab.bankingapplication.R
import com.ab.bankingapplication.util.Constants.LOCATION_PERMISSION_REQUEST_CODE
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

    fun  Double.getAmountWithCurrency(): String {
        return BankingApplication.currencySymbol.plus(this.toString())
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

    fun setTextBlackAndBold(originalText: String, startIndex: Int, endIndex: Int): SpannableStringBuilder {
        val spannableStringBuilder = SpannableStringBuilder(originalText)
        val foregroundColorSpan = ForegroundColorSpan(Color.BLACK)
        spannableStringBuilder.setSpan(foregroundColorSpan, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        val styleSpan = StyleSpan(Typeface.BOLD)
        spannableStringBuilder.setSpan(styleSpan, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        return spannableStringBuilder
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

    fun isLocationPermissionAndLocationEnabled(context: Context,activity: Activity) : Boolean {
        var bothPermissionAndLocationAvailable = false
        when{
            !isLocationPermissionGranted(context) && !shouldShowLocationPermissionRationale(activity) -> showLocationPermissionDeniedDialog(activity,context)
            !isLocationPermissionGranted(context) -> requestLocationPermission(activity)
            !isLocationEnabled(context) -> showLocationEnableDialog(activity)
            else -> bothPermissionAndLocationAvailable = true
        }
        return bothPermissionAndLocationAvailable
    }


    private fun isLocationPermissionGranted(context: Context): Boolean {
        val permission = ACCESS_FINE_LOCATION
        val granted = PackageManager.PERMISSION_GRANTED
        return ContextCompat.checkSelfPermission(context, permission) == granted
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestLocationPermission(activity: Activity) {
        val permission = ACCESS_FINE_LOCATION
        ActivityCompat.requestPermissions(activity, arrayOf(permission), LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun shouldShowLocationPermissionRationale(activity: Activity): Boolean {
        val permission = ACCESS_FINE_LOCATION
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    private fun showLocationPermissionDeniedDialog(activity: Activity,context: Context) {
        showAlertDialog(activity,"Permission Required","To provide accurate location information, the app needs access to your location.","Grant Permission"){
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
    }

    private fun showLocationEnableDialog(activity: Activity) {
        showAlertDialog(activity,"Location Services Disabled","Please enable location services to use this feature.","Enable") {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity.startActivity(intent)
        }
    }

    private fun showAlertDialog(activity: Activity, title: String,message : String,positiveButton : String,positiveButtonAction : (Unit) -> Unit = { }){
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { _, _ ->
                positiveButtonAction(Unit)
            }
            .show()
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