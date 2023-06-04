package com.ab.bankingapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ab.bankingapplication.databinding.FragmentFirstBinding
import com.ab.bankingapplication.model.CardDetails
import com.ab.bankingapplication.model.CurrenciesModel
import com.ab.bankingapplication.util.AdapterUtils
import com.jakewharton.rxbinding.view.clicks
import rx.android.schedulers.AndroidSchedulers
import java.util.Date

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class BankHomeFragment : Fragment() {

    lateinit var binding: FragmentFirstBinding
    private lateinit var viewModel: BankHomeViewModel



    private fun bind(){
        binding.sendIconLayout.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BankHomeViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
        bindLayout()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun getTransactionList(){
        viewModel.fetchTransaction()
        viewModel.transaction.observe(viewLifecycleOwner) { transaction ->
            // Update UI with the currency data
            bindTransactionList(transaction)
        }
    }

    private fun bindLayout(){
        bindCardList()
        getTransactionList()
    }

    private fun bindCardList(){
        val cardList = listOf(
            CardDetails(1,"American Express",9345.05,"1234 5678 9123 0543",Date(),R.color.blue),
            CardDetails(1,"Master Card",1245.05,"1234 5678 9123 9246",Date(),R.color.black)
        )

        binding.cardList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL,false)
        binding.cardList.adapter = AdapterUtils.setUpCardListAdapter(cardList)
    }

    private fun bindTransactionList(transaction : List<CurrenciesModel>){
        binding.transactionList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL,false)
        binding.transactionList.adapter = AdapterUtils.setUpTransactionListAdapter(transaction)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}