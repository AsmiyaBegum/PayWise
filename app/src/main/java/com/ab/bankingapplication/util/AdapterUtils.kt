package com.ab.bankingapplication.util

import com.ab.bankingapplication.R
import com.ab.bankingapplication.databinding.CardListRowBinding
import com.ab.bankingapplication.databinding.TransactionListRowBinding
import com.ab.bankingapplication.model.CardDetails
import com.ab.bankingapplication.model.CurrenciesModel
import com.ab.bankingapplication.util.Utils.formatDate
import com.ab.bankingapplication.util.Utils.maskCardNumber
import com.jakewharton.rxbinding.view.clicks
import rx.android.schedulers.AndroidSchedulers

object AdapterUtils {

    fun setUpCardListAdapter(subjectList : List<CardDetails>) : GenericAdapter<CardDetails, CardListRowBinding,List<CardDetails>>{

        val adapter = GenericAdapter(R.layout.card_list_row,object : GenericAdapterInteraction<CardDetails, CardListRowBinding,List<CardDetails>>(){

            override fun bindingViewHolder(binding: CardListRowBinding, data: CardDetails,
                                           holder: GenericAdapter.GenericViewHolder<CardDetails, CardListRowBinding, List<CardDetails>>, additionalData: List<CardDetails>?) {

                    binding.cardDetails = data
                    binding.creditCardNumber.maskCardNumber(data.creditCardNumber,true,data.creditCardNumber.length-4)
                    binding.expiredDate.formatDate(data.creditCardExpiredDate,Constants.DATE_FORMAT_MMDD)
//                    binding.cardBalanceAmount.formatAmount(data.cardBalance)
                    binding.cardBalanceAmount.maskCardNumber(data.cardBalance.toString(),true)
                    binding.maskBalance.clicks()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            val maskedBalance = binding.cardBalanceAmount.text.toString().contains("*")
                            binding.cardBalanceAmount.maskCardNumber(data.cardBalance.toString(),!maskedBalance)
                            changeMaskBalanceIcon(!maskedBalance,binding)
                        }
            }


            private fun changeMaskBalanceIcon(showMaskEye : Boolean,binding: CardListRowBinding){
              val eyeMask =  if(showMaskEye){
                    binding.root.resources.getDrawable(R.drawable.ic_eye_off)
                }else{
                    binding.root.resources.getDrawable(R.drawable.ic_eye)
                }
                binding.maskBalance.setImageDrawable(eyeMask)
            }

            override fun onClicked(data: CardDetails,binding: CardListRowBinding) {
                //override fun not implemented
            }

        })
        adapter.addItems(subjectList)

        return adapter
    }

    fun setUpTransactionListAdapter(currencyList : List<CurrenciesModel>) : GenericAdapter<CurrenciesModel, TransactionListRowBinding,List<Unit>>{

        val adapter = GenericAdapter(R.layout.transaction_list_row,object : GenericAdapterInteraction<CurrenciesModel, TransactionListRowBinding,List<Unit>>(){

            override fun bindingViewHolder(
                binding: TransactionListRowBinding,
                data: CurrenciesModel,
                holder: GenericAdapter.GenericViewHolder<CurrenciesModel, TransactionListRowBinding, List<Unit>>,
                additionalData: List<Unit>?
            ) {
                binding.transactionType.text = data.sName
                binding.transactionTime.text = data.sISOCode
            }

            override fun onClicked(data: CurrenciesModel, binding: TransactionListRowBinding) {
                // override fun not implemented
            }
        })
        adapter.addItems(currencyList)

        return adapter
    }

}
