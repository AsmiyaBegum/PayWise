package com.ab.bankingapplication.moneySend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ab.bankingapplication.MainActivity
import com.ab.bankingapplication.R
import com.ab.bankingapplication.databinding.TransactionSuccessPageBinding
import com.ab.bankingapplication.model.TransactionDetails
import com.ab.bankingapplication.util.Constants
import com.ab.bankingapplication.util.Utils.formatDate
import com.ab.bankingapplication.util.Utils.getAmountWithCurrency
import com.jakewharton.rxbinding.view.clicks
import rx.Observable
import rx.android.schedulers.AndroidSchedulers

class TransactionSuccessFragment : Fragment() {

    lateinit var binding: TransactionSuccessPageBinding
    lateinit var transactionDetails: TransactionDetails

    private fun bind(){

        Observable.merge(binding.backButton.clicks(),binding.backToHomeButton.clicks())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                findNavController().navigate(R.id.action_TransactionSuccessFragment_to_BankHomeFragment)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        transactionDetails = requireArguments().getParcelable(Constants.TRANSACTION_DETAIL_KEY)?:TransactionDetails()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TransactionSuccessPageBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).showOrHideNavigationView(false)
        bindData()
        bind()
    }

    private fun bindData(){
        binding.transactionDetailData = transactionDetails
        binding.amountSent.text = transactionDetails.amount.getAmountWithCurrency()
        binding.transactionFee.text = transactionDetails.transactionFee.getAmountWithCurrency()
        binding.amount.text = transactionDetails.amount.getAmountWithCurrency()
        binding.transactionDate.formatDate(transactionDetails.transactionDate,Constants.DATE_FORMAT_DDMMYYY)
        binding.transactionTime.formatDate(transactionDetails.transactionDate,Constants.TIME_FORMAT_HHMMSSA)
    }

}