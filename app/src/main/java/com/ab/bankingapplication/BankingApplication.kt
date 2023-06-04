package com.ab.bankingapplication

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.ab.bankingapplication.util.Constants
import com.ab.bankingapplication.util.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import rx.subjects.PublishSubject
import java.util.*
import kotlin.system.exitProcess

class BankingApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        if(!Utils.isBuildVariantDebug()){
            Thread.setDefaultUncaughtExceptionHandler { _, e ->
                handleUncaughtException(e)
                restartApp()
            }
        }
    }


    private fun handleUncaughtException(e : Throwable){
        Log.e("app_crash",e.message.toString())
    }

    private fun restartApp() {
        val intent = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        exitProcess(1)
    }



    companion object{
        private var appContext: Context? = null

        val deviceOfflineSubject = PublishSubject.create<Boolean>()
        private lateinit var fusedLocationClient: FusedLocationProviderClient
        var currencySymbol : String = "$"
        private var currentLocation : String = ""

        fun appContext(): Context? {
            return appContext
        }

        fun securedSharedPref() : SharedPreferences {
            val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
            val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
            return EncryptedSharedPreferences.create(Constants.APP_NAME, masterKeyAlias, appContext!!, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        }

        fun fetchLocation() {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext!!)
            if (ActivityCompat.checkSelfPermission(appContext!!, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(appContext!!, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            updateLocationAndCurrency(location)
                        }
                    }
            }

        }

        private fun updateLocationAndCurrency(location: Location) {
            val context: Context = appContext!!
            getCurrencyCodeFromLocation(context,location)
        }

        private fun getCurrencyCodeFromLocation(context: Context, location : Location){
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses!!.isNotEmpty()) {
                    val countryCode = addresses[0].countryCode
                    val locale = Locale("", countryCode)
                    val currencyCode = Currency.getInstance(locale).currencyCode
                    val currency = Currency.getInstance(currencyCode)
                    currencySymbol = currency.symbol
                    currentLocation = addresses[0].getAddressLine(0)
                }
            }catch (e : Exception)
            {
                Log.e("location_fetch","Failed to fetch currency and location")
            }

        }

    }
}