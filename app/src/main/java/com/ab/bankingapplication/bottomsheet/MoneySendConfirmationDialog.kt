package com.ab.bankingapplication.bottomsheet

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.ab.bankingapplication.R
import com.ab.bankingapplication.databinding.MoneySendConfirmationDialogBinding
import com.ab.bankingapplication.model.TransactionDetails
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding.view.clicks
import rx.android.schedulers.AndroidSchedulers

class MoneySendConfirmationDialog :  BottomSheetDialogFragment() {


    lateinit var binding : MoneySendConfirmationDialogBinding
    lateinit var delegate: Delegate
    lateinit var transactionDetails: TransactionDetails

    interface Delegate {
        fun confirmPayment()
    }
    private fun bind() {

        binding.cancelButton.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                dialog?.dismiss()
            }

        binding.sendButton.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                delegate.confirmPayment()
                dialog?.dismiss()
            }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MoneySendConfirmationDialogBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindDataToLayout()
        bind()
    }

    private fun bindDataToLayout(){
        binding.transactionDetails = transactionDetails
    }

    companion object {
        fun newInstance(transactionDetails: TransactionDetails,delegate : Delegate) : MoneySendConfirmationDialog{
            val dialog = MoneySendConfirmationDialog()
            dialog.transactionDetails = transactionDetails
            dialog.delegate = delegate
            return dialog
        }
    }
}