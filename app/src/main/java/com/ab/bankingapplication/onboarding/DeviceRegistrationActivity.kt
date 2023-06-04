package com.ab.bankingapplication.onboarding

import com.twilio.Twilio
import com.twilio.rest.verify.v2.service.Verification

import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.ab.bankingapplication.MainActivity
import com.ab.bankingapplication.R
import com.ab.bankingapplication.databinding.ActivityDeviceRegistrationBinding
import com.ab.bankingapplication.util.Constants
import com.ab.bankingapplication.util.Utils
import com.ab.bankingapplication.util.Utils.openKeyboard
import com.ab.bankingapplication.util.Utils.setTextBlackAndBold
import com.ab.bankingapplication.util.Utils.showVisibility
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding.view.changeEvents
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.textChangeEvents
import com.twilio.rest.api.v2010.account.incomingphonenumber.Local.creator
import com.twilio.rest.api.v2010.account.incomingphonenumber.TollFree.creator

import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import rx.Observable
import rx.android.schedulers.AndroidSchedulers


class DeviceRegistrationActivity :  AppCompatActivity(){

    private lateinit var binding: ActivityDeviceRegistrationBinding
    private lateinit var viewModel: RegistrationViewModel

    val statusList = arrayOf(Constants.HOME_TOUR_1,Constants.HOME_TOUR_2)
    private var tabStatus = Constants.HOME_TOUR_1

    private val ACCOUNT_SID = "AC5a6d58ab630fdf628913f4aee008dffa"
    private val AUTH_TOKEN = "5aec95c5d2693d596ad302135bfc29be"
    private val VERIFY_SERVICE_SID = "VAfee515d3017edbe9056bf95f81b1dcb4"

    private val smsConsentRequest = 526
    private lateinit var smsReceiver: BroadcastReceiver
    private var countDownTimer : CountDownTimer? = null
    private var invalidCount : Int = 0



    private var snackBarOpened : Snackbar? = null

    private val snackBar : (Snackbar) -> Unit ={ snack ->
        snackBarOpened = snack
    }

    private fun dismissPreviousSnackBar(){
        snackBarOpened?.dismiss()
    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals("otp", ignoreCase = true)) {
                val otpValue = intent.getStringExtra("otpValue")
                if (otpValue != null) {
                    binding.otpView.setOTP(otpValue)
                }
            }
        }
    }

    private fun bind(){
        
        binding.nextButton.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                handleNextButtonEvent()
            }

        binding.resendOTP.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                generateOTPAndSendSMS()
                validateAndInitiateTimer()
            }


        binding.otpButton.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if(Utils.checkInternetConnection()){
                    if(binding.mobileLoginPage.isVisible){
                        if(Utils.isIndianMobileNumber(binding.mobileNumber.text.toString())){
                            generateOTPAndSendSMS()
                        }else{
                            Utils.snackBarListener(binding.otpButton,getString(R.string.enter_valid_mob_num),snackBar)
                        }
                    }else{
                        validateOTP()
                    }
                }else{
                    Utils.snackBarListener(binding.otpButton,getString(R.string.device_offline),snackBar)
                }
            }

        
        Observable.merge(binding.mobileNumber.textChangeEvents(),binding.otpView.changeEvents())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { 
                dismissPreviousSnackBar()
            }
        

    }

    private fun validateOTP(){
        val enteredOTP = binding.otpView.otp?:""
        if(enteredOTP.isBlank()){
            Utils.snackBarListener(binding.otpButton, getString(R.string.enter_otp),snackBar)
        }else if (isOTPExpired()){
            showMobileLogin(true)
            Utils.snackBarListener(binding.otpButton,getString(R.string.otp_expired),snackBar)
        }else if(!isOTPValid(enteredOTP)){
            invalidCount += 1
            if(invalidCount >=5){
                Utils.snackBarListener(binding.otpButton,getString(R.string.maximum_otp_trials_exceeded),snackBar)
                showMobileLogin(true)
                validateTimeAndInitiateCount(System.currentTimeMillis() + 30000L)
                invalidCount = 0

            }else{
                Utils.snackBarListener(binding.otpButton,getString(R.string.invalid_otp),snackBar)
            }
            binding.otpView.setOTP("")
        }else{
            routeToBankApplicationActivity()
        }
    }

    private fun isOTPValid(enteredOTP : String) : Boolean{
        val generatedOTP = Utils.secureSharedPreferences().getString(Constants.SHARED_PREF_GENERATED_OTP,"").toString()
        return generatedOTP.isNotBlank() && generatedOTP == enteredOTP
    }

    private fun isOTPExpired() : Boolean{
        val otpValidTime = Utils.sharedPreferences().getString(Constants.SHARED_PREF_VERIFY_OTP_LAST_REQUEST_TIME,"0")!!.toLong()
        return otpValidTime < System.currentTimeMillis()
    }

    private fun routeToBankApplicationActivity(){
        Utils.setAppState(Constants.APP_STATE_LOGGED_IN)
        resetVerifyOTPRequestTimeAndGeneratedOTP()
        startMainActivity()
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun resetVerifyOTPRequestTimeAndGeneratedOTP(){
        // reset generated otp and time
        Utils.setSecureSharedPreferences(Constants.SHARED_PREF_GENERATED_OTP,"")
        Utils.setSharedPrefStr(Constants.SHARED_PREF_VERIFY_OTP_LAST_REQUEST_TIME,"0")
    }


    private fun generateOTPAndSendSMS(){
//        if (binding.mobileNumber.text.toString().contains(Constants.DEMO_MOBILE_NUMBER)){
            Utils.setSecureSharedPreferences(Constants.SHARED_PREF_GENERATED_OTP,Constants.DEMO_OTP)
//        } else {
//            val otp = Utils.generateRandomOTP()
//            Utils.setSecureSharedPreferences(Constants.SHARED_PREF_GENERATED_OTP,otp)
//            sendVerificationCode(binding.mobileNumber.text.toString())
//        }
        Utils.setAppState(Constants.APP_STATE_VERIFY_OTP)
        storeMobileNumber()
        showMobileLogin(false)
        // set validTime
        Utils.setSharedPrefStr(Constants.SHARED_PREF_VERIFY_OTP_LAST_REQUEST_TIME, (System.currentTimeMillis() + 30000).toString())
    }

    private fun storeMobileNumber(){
        Utils.setSecureSharedPreferences(Constants.SHARED_PREF_USER_MOBILE_NUMBER,binding.mobileNumber.text.toString())
    }

    private fun sendMessage(to: String?, message: String?) {
        val mes = Message.creator(PhoneNumber(to), PhoneNumber(Constants.TWILIO_PHONE_NUMBER), message).create()
        mes.runCatching {
            println(this)
        }
    }



    private fun sendVerificationCode(phoneNumber: String) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN)
        sendMessage(phoneNumber,"sample")
    }



    private fun handleNextButtonEvent() {
        val nextStatus = getTabStatus()
        if (tabStatus != Constants.HOME_TOUR_2) {
            tabStatus = nextStatus
            binding.tabLayout.getTabAt(getCurrentPosition())?.select()
        } else {
            Utils.setAppState(Constants.APP_STATE_LOGIN_PAGE)
            handleFormVisibility()
        }
    }

    private fun getTabStatus(): String {
        val currentIndex = statusList.indexOf(tabStatus)
        return if (currentIndex < statusList.size - 1) {
            statusList[currentIndex + 1]
        } else {
            statusList[0]
        }
    }

    private fun getCurrentPosition() : Int {
        return statusList.indexOfFirst { it == tabStatus }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeviceRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bind()

        // Initialize the ViewModel using the ViewModelProvider
        viewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]
    }

    override fun onResume() {
        bindLayout()
        super.onResume()
    }


    private fun bindLayout(){
        handleFormVisibility()
        validateAndInitiateTimer()
    }


    private fun validateAndInitiateTimer(){
        val verifyOTPLastReqTime = Utils.sharedPreferences().getString(Constants.SHARED_PREF_VERIFY_OTP_LAST_REQUEST_TIME,"")?.toLongOrNull()?:0L
        if(verifyOTPLastReqTime != null){
            validateTimeAndInitiateCount(verifyOTPLastReqTime)
        }

    }

    private fun validateTimeAndInitiateCount(endTime : Long){
        val currentTime = System.currentTimeMillis()
        if(endTime > currentTime){
            val diffTime = endTime - currentTime
            initCountDownTimer(diffTime)
        }else{
          resetVerifyOTPRequestTimeAndGeneratedOTP()
        }
    }

    private fun initCountDownTimer(time: Long) {
        if(countDownTimer != null) {
            countDownTimer!!.cancel()
            countDownTimer = null
        }
        countDownTimer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if(binding.mobileLoginPage.isVisible){
                    binding.resendText.showVisibility(true)
                    binding.otpButton.isEnabled = false
                    binding.resendText?.text = resources.getString(R.string.resend_otp_time, (millisUntilFinished/1000).toString())
                    resendOtpDefault()
                }else{
                    binding.otpButton.isEnabled = true
                    binding.resendOTP.isEnabled = false
                    binding.resendOTP?.text = resources.getString(R.string.resend_otp_time,(millisUntilFinished / 1000).toString())
                }
            }

            override fun onFinish() {
                if(binding.mobileLoginPage.isVisible){
                    Utils.setSharedPrefStr(Constants.SHARED_PREF_VERIFY_OTP_LAST_REQUEST_TIME,"")
                    binding.resendText.showVisibility(false)
                    binding.resendText.isEnabled = true
                    binding.resendOTP.isEnabled = true
                } else {
                    resendOtpDefault()
                    Utils.setSharedPrefStr(Constants.SHARED_PREF_VERIFY_OTP_LAST_REQUEST_TIME,"")
                }
                binding.otpButton.isEnabled = true

            }
        }.start()
    }



    private fun resendOtpDefault() {
        binding.resendOTP.isEnabled = true
        binding.resendOTP?.text = resources.getString(R.string.resend_otp)
    }


    private fun handleFormVisibility(){
        val appState = Utils.secureSharedPreferences().getString(Constants.SHARED_PREF_APP_STATE,Constants.APP_STATE_HOME_TOUR)
        Log.d("asmi",appState.toString())
        when(appState){
            Constants.APP_STATE_HOME_TOUR -> {
                showHomeTour(true)
                Utils.setAppState(Constants.APP_STATE_HOME_TOUR)
            }
            Constants.APP_STATE_LOGIN_PAGE -> {
                showHomeTour(false)
                showMobileLogin(true)
            }
            Constants.APP_STATE_VERIFY_OTP -> {
                showHomeTour(false)
                showMobileLogin(true)
            }
            Constants.APP_STATE_LOGGED_IN -> {
                startMainActivity()
            }
        }
    }


    private fun showMobileLogin(show : Boolean){
        binding.mobileLoginPage.showVisibility(show)
        binding.otpPage.showVisibility(!show)

        if(!show){
            if(Build.VERSION.SDK_INT > 25){
                registerSMSConsent()
            } else {
                binding.otpView.openKeyboard(true,applicationContext)
            }
            val msg = resources.getString(R.string.otp_sent_message,Utils.getUserMobileNumber())
            binding.sendOtpMsg.text = setTextBlackAndBold(msg,22,msg.length)
            binding.otpButton.text = resources.getString(R.string.verify_and_proceed)
        }else{
            binding.mobileNumber.setText(Utils.getUserMobileNumber())
            binding.sendOtpMsg.text = setTextBlackAndBold( resources.getString(R.string.send_otp_for_mobile_number_msg),20,37)
            binding.otpButton.text = resources.getString(R.string.verify_otp)
        }
        //handle timer

    }


    private fun showHomeTour(show : Boolean){
        binding.loginHomeTour.showVisibility(show)
        binding.loginScreen.showVisibility(!show)
        bindHomeTourViewPager(show)
    }

    private fun bindHomeTourViewPager(show : Boolean){
        if(show){
            binding.homeTourViewPager.adapter = HomeTourAdapter(supportFragmentManager,statusList)
            binding.tabLayout.setupWithViewPager(binding.homeTourViewPager, true)
            binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val position = tab.position
                    tabStatus = statusList[position]
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    //override fun not implemented

                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    //override fun not implemented
                }


            })
        }
    }


    private fun registerSMSConsent(){
        if(Build.VERSION.SDK_INT > 25){
            SmsRetriever.getClient(this).startSmsUserConsent(null)
            val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            smsReceiver = smsVerificationReceiver
            registerReceiver(smsReceiver,intentFilter,SmsRetriever.SEND_PERMISSION,null)
        }
    }


    private val smsVerificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

                when (smsRetrieverStatus.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val consentIntent =
                            extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                        try {
                            if (consentIntent != null) {
                                startActivityForResult(consentIntent, smsConsentRequest)
                            }
                        } catch (e: ActivityNotFoundException) {
                            Utils.logMessage("smsConsent",e.toString(),Constants.ERROR_MODE)
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            smsConsentRequest -> {
                val receivedOtp = data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE).toString().filter { it.isDigit() }
                if(receivedOtp.length == 5){
                    binding.otpView.setOTP(receivedOtp)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(this::smsReceiver.isInitialized){
            unregisterReceiver(smsVerificationReceiver)
        }
    }


    override fun onBackPressed() {
        if (binding.otpPage.isVisible) {
            Utils.setSharedPrefStr(Constants.SHARED_PREF_APP_STATE, Constants.APP_STATE_LOGIN_PAGE)
            showMobileLogin(true)
            validateAndInitiateTimer()
        } else {
            super.onBackPressed()
        }
    }


}

class HomeTourAdapter(
    fragmentManager: FragmentManager,
    private val statusList: Array<String>
) : FragmentStatePagerAdapter(fragmentManager) {
    override fun getCount(): Int = statusList.size

    override fun getItem(position: Int): Fragment {
        return HomeTourFragment.newInstance(statusList[position])
    }

}

