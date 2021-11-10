package com.geek.mrguard.UI.login_signUp

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.geek.mrguard.R
import com.geek.mrguard.data.signInResponse
import com.geek.mrguard.databinding.FragmentOtpVerificationBSListDialogBinding
import com.geek.mrguard.viewModel.LoginViewModel


class OtpVerificationBS : BottomSheetDialogFragment() {

    private var _binding: FragmentOtpVerificationBSListDialogBinding? = null
    private val binding get() = _binding!!
    val model : LoginViewModel by activityViewModels()
    var res = MutableLiveData<signInResponse>()
    var finalRes = MutableLiveData<signInResponse>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpVerificationBSListDialogBinding.inflate(inflater, container, false)
        binding.verifyOTP.setOnClickListener {
            model.setOTP(binding.OTP.text.toString())
            model.login()
        }
        binding.resendBtn.setOnClickListener{
            model.getOTP()
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val touchOutsideView = dialog!!.window
            ?.decorView
            ?.findViewById<View>(R.id.touch_outside)
        touchOutsideView?.setOnClickListener(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}