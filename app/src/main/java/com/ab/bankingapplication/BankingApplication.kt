package com.ab.bankingapplication

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.ab.bankingapplication.util.Constants
import com.ab.bankingapplication.util.Utils
import rx.subjects.PublishSubject
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

        fun appContext(): Context? {
            return appContext
        }

        fun securedSharedPref() : SharedPreferences {
            val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
            val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
            return EncryptedSharedPreferences.create(Constants.APP_NAME, masterKeyAlias, appContext!!, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        }

    }
}