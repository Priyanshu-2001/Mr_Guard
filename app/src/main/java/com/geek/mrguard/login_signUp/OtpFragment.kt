package com.geek.mrguard.login_signUp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geek.mrguard.R
import com.geek.mrguard.databinding.FragmentOtpFragmentBinding
import com.geek.mrguard.viewModel.LoginViewModel

class OtpFragment : Fragment() {
    lateinit var binding : FragmentOtpFragmentBinding
    lateinit var model : ViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_otp_fragment,container , false)
        model = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding.sendOTP.setOnClickListener {
            OtpVerificationBS().show(parentFragmentManager,"verify")
        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = OtpFragment()
    }
}