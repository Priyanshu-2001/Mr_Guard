package com.geek.mrguard.login_signUp

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.geek.mrguard.databinding.FragmentOtpVerificationBSListDialogBinding


class OtpVerificationBS : BottomSheetDialogFragment() {

    private var _binding: FragmentOtpVerificationBSListDialogBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpVerificationBSListDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    companion object {
        fun newInstance(itemCount: Int): OtpVerificationBS =OtpVerificationBS()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}