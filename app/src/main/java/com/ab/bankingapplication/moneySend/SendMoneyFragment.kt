package com.ab.bankingapplication.moneySend

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ab.bankingapplication.MainActivity
import com.ab.bankingapplication.R
import com.ab.bankingapplication.bottomsheet.MoneySendConfirmationDialog
import com.ab.bankingapplication.databinding.FragmentSendMoneyBinding
import com.ab.bankingapplication.model.TransactionDetails
import com.ab.bankingapplication.util.Constants
import com.ab.bankingapplication.util.Utils
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.textChangeEvents
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.Date

/**
 * A Send Money screen to send money via phone number.
 */
class SendMoneyFragment : Fragment(),MoneySendConfirmationDialog.Delegate {

    lateinit var binding: FragmentSendMoneyBinding

    private var snackBarOpened : Snackbar? = null

    private val snackBar : (Snackbar) -> Unit ={ snack ->
        snackBarOpened = snack
    }

    private lateinit var transactionDetails : TransactionDetails

    private fun dismissPreviousSnackBar(){
        snackBarOpened?.dismiss()
    }


    private fun bind(){

        binding.sendMoneyButton.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                validateBeforeSendMoney()
            }

        Observable.merge(binding.recipientName.textChangeEvents(),binding.amount.textChangeEvents(),binding.mobileNumber.textChangeEvents())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                dismissPreviousSnackBar()
            }

        binding.backButton.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                findNavController().navigate(R.id.action_SendMoneyFragment_to_BankHomeFragment)
            }
    }


    private fun validateBeforeSendMoney(){
        val result = when{
            !Utils.checkInternetConnection() -> getString(R.string.device_offline)
            binding.recipientName.text.isNullOrBlank() -> getString(R.string.provide_recipient)
            binding.mobileNumber.text.isNullOrBlank() || !Utils.isIndianMobileNumber(binding.mobileNumber.text.toString())-> getString(
                R.string.provide_valid_mobile_number
            )
            binding.amount.text.isNullOrBlank() || binding.amount.text.toString().toDouble() <= 0 -> getString(
                R.string.provide_valid_amount
            )
            else -> Constants.VALIDATION_SUCCESS
        }
        if(result == Constants.VALIDATION_SUCCESS){
            confirmMoneySend()
        }else{
            Utils.snackBarListener(binding.sendMoneyButton,result,snackBar)
        }
    }


    private fun confirmMoneySend(){
        transactionDetails = TransactionDetails(Constants.USER_NAME,binding.recipientName.text.toString(),Utils.getUserMobileNumber(),binding.mobileNumber.text.toString(),binding.amount.text.toString().toDouble())
        val dialog = MoneySendConfirmationDialog.newInstance(transactionDetails,this)
        dialog.show(requireFragmentManager(), Constants.MONEY_SEND_CONFIRMATION_DIALOG_TAG)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSendMoneyBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
        (activity as MainActivity).showOrHideNavigationView(false)
    }

    override fun confirmPayment() {
        /*
        Send the money to the number. Once send the number , show transaction succeess screen
         */

        transactionDetails.transactionDate = Date()
        transactionDetails.transactionNumber = Constants.TRANSACTION_NUMBER
        transactionDetails.transactionFee = 10.40

        val bundle = Bundle()
        bundle.putParcelable(Constants.TRANSACTION_DETAIL_KEY,transactionDetails)

        findNavController().navigate(R.id.action_SendMoneyFragment_to_TransactionSuccessFragment,bundle)
    }

}