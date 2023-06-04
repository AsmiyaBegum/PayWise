package com.ab.bankingapplication.bankHome

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ab.bankingapplication.BankingApplication
import com.ab.bankingapplication.MainActivity
import com.ab.bankingapplication.R
import com.ab.bankingapplication.databinding.FragmentBankHomeBinding
import com.ab.bankingapplication.model.CardDetails
import com.ab.bankingapplication.model.CurrenciesModel
import com.ab.bankingapplication.util.AdapterUtils
import com.ab.bankingapplication.util.Constants
import com.ab.bankingapplication.util.Utils
import com.ab.bankingapplication.util.Utils.openKeyboard
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.editorActionEvents
import rx.android.schedulers.AndroidSchedulers
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * A Bank home screen dispaying features and transaction list
 */
class BankHomeFragment : Fragment() {

    lateinit var binding: FragmentBankHomeBinding
    private lateinit var viewModel: BankHomeViewModel
    lateinit var searchTextView: AutoCompleteTextView

    private var transactionList : List<CurrenciesModel> = listOf()


    private fun bind(){
        binding.sendIconLayout.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                openFragmentAfterLocationPermissionGranted()
            }

        searchTextView.editorActionEvents()
            .map { searchText ->
                searchText
            }
            .debounce(220, TimeUnit.MILLISECONDS)
            .filter { searchTextView.text.isNotBlank() }
            .filter { editorActionEvent ->
                val keyEvent = editorActionEvent.keyEvent()
                keyEvent == null || (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.action == KeyEvent.ACTION_UP)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { editorActionEvent ->
                filterTransaction(editorActionEvent.keyEvent().toString())
            }
    }


    private fun filterTransaction(searchText : String) {
        bindTransactionList(transactionList.filter { it.sISOCode.contains(searchText) || it.sName.contains(searchText) })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentBankHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BankHomeViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customiseSearchLayout()
        bind()
        searchTextView.openKeyboard(false,requireContext())
    }

    override fun onResume() {
        super.onResume()
        bindLayout()
    }

    private fun getTransactionList(){
        viewModel.fetchTransaction()
        viewModel.transaction.observe(viewLifecycleOwner) { transaction ->
            // Update UI with the currency data
            transactionList = transaction
            bindTransactionList(transaction)
        }
    }

    private fun bindLayout(){
        bindCardList()
        getTransactionList()
        (activity as MainActivity).showOrHideNavigationView(true)
    }

    private fun customiseSearchLayout(){
            val searchTextId = binding.searchView.context.resources.getIdentifier("android:id/search_src_text", null, null)
            searchTextView = binding.searchView?.findViewById<AutoCompleteTextView>(searchTextId) as AutoCompleteTextView
            searchTextView.textSize = Constants.DEFAULT_SEARCH_TEXT_SIZE
    }

    private fun bindCardList(){
        val cardList = listOf(
            CardDetails(1,"American Express",9345.05,"1234 5678 9123 0543",Date(),
                R.color.purple_500
            ),
            CardDetails(1,"Master Card",1245.05,"1234 5678 9123 9246",Date(), R.color.black)
        )

        binding.cardList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL,false)
        binding.cardList.adapter = AdapterUtils.setUpCardListAdapter(cardList)
    }

    private fun bindTransactionList(transaction : List<CurrenciesModel>){
        binding.transactionList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL,false)
        binding.transactionList.adapter = AdapterUtils.setUpTransactionListAdapter(transaction)
    }

    private fun openFragmentAfterLocationPermissionGranted() {
        if(Utils.isLocationPermissionAndLocationEnabled(requireContext(),requireActivity())){
            BankingApplication.fetchLocation()
            findNavController().navigate(R.id.action_BankHomeFragment_to_SendMoneyFragment)
        }

    }

    private fun handleLocationPermissionResult(requestCode: Int) {
        if (requestCode == Constants.LOCATION_PERMISSION_REQUEST_CODE) {
            openFragmentAfterLocationPermissionGranted()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        handleLocationPermissionResult(requestCode)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}