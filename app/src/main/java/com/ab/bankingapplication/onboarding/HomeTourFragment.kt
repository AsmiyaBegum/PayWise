package com.ab.bankingapplication.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ab.bankingapplication.R
import com.ab.bankingapplication.databinding.HomeTourListPageBinding
import com.ab.bankingapplication.util.Constants

class HomeTourFragment : Fragment() {


    lateinit var binding : HomeTourListPageBinding
    private var pageName : String = Constants.HOME_TOUR_1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeTourListPageBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindQRInstructionWithImage()
    }


    private fun bindQRInstructionWithImage(){
            when(pageName){
                Constants.HOME_TOUR_1 ->{
                    bindQrInstruction(getString(R.string.home_tour1),R.drawable.app_home_tour1)
                }
                Constants.HOME_TOUR_2 ->{
                    bindQrInstruction( getString(R.string.home_tour2),R.drawable.app_home_tour2)
                }
            }

    }

    private fun bindQrInstruction(string: String,drawable : Int){
        binding.homeTourContent.text = string
        binding.homeTourImage.setImageDrawable(resources.getDrawable(drawable))
    }

    companion object {
        fun newInstance(pageName : String)  : HomeTourFragment{
            val fragment = HomeTourFragment()
            fragment.pageName = pageName
            return fragment
        }
    }
}