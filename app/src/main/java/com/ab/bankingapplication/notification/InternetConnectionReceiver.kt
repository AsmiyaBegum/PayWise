package com.ab.bankingapplication.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ab.bankingapplication.BankingApplication
import com.ab.bankingapplication.util.Constants
import com.ab.bankingapplication.util.Utils

class InternetConnectionReceiver(): BroadcastReceiver() {

    val TAG = "InternetConnectionRecvr"


    override fun onReceive(context: Context?, intent: Intent?) {

        val connected = Utils.checkInternetConnection()
        if (connected) {
            Utils.logMessage(TAG, "Executing data sync", Constants.DEBUG_MODE)
        }
        if (!connected && Utils.isAppInForeground(context)) {
            BankingApplication.deviceOfflineSubject.onNext(true)
        } else if (connected && Utils.isAppInForeground(context)) {
            BankingApplication.deviceOfflineSubject.onNext(false)
        }

    }
}
