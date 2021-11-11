package com.geek.mrguard.UI.login_signUp

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geek.mrguard.R
import com.geek.mrguard.databinding.FragmentOtpFragmentBinding
import com.geek.mrguard.guardApplication
import com.geek.mrguard.services.LoginService
import com.geek.mrguard.viewModel.LoginViewModel
import com.geek.mrguard.viewModel.LoginViewModelFactory

class OtpFragment : Fragment() {
    lateinit var binding : FragmentOtpFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_otp_fragment,container , false)
//        model = ViewModelProvider(this,LoginViewModelFactory(repo)).get(LoginViewModel::class.java)
        val repo = (requireActivity().application as guardApplication).loginRepo
        val model : LoginViewModel by activityViewModels {
            LoginViewModelFactory(repo,requireContext())
        }
        binding.sendOTP.setOnClickListener {
            val phoneNumber = binding.phoneNumber.text.toString()
            model.phoneNumber.value = phoneNumber
            Log.e("TAG", "onCreateView: $phoneNumber")
            if(phoneNumber.isNotEmpty() && phoneNumber.length==10){
                model.getOTP()
                OtpVerificationBS().show(parentFragmentManager,"verify")
            }
            else{
                Toast.makeText(context, "Invalid Credentials !!", Toast.LENGTH_SHORT).show()
            }

        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = OtpFragment()
    }
}