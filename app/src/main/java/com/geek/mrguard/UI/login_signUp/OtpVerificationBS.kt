package com.geek.mrguard.UI.login_signUp

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.geek.mrguard.R
import com.geek.mrguard.UI.dashBorad.DashBoard
import com.geek.mrguard.databinding.FragmentOtpVerificationBSListDialogBinding


class OtpVerificationBS : BottomSheetDialogFragment() {

    private var _binding: FragmentOtpVerificationBSListDialogBinding? = null

    private val binding get() = _binding!!

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
            nextScreenIntent()
        }
        return binding.root
    }

    private fun nextScreenIntent() {
        startActivity(Intent(context, DashBoard::class.java))
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