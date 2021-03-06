package com.geek.mrguard.UI.login_signUp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.geek.mrguard.R
import com.geek.mrguard.UI.dashBoard.Police.PoliceDashBoard
import com.geek.mrguard.databinding.FragmentLoginFragBinding

class LoginFragment : Fragment() {

    lateinit var binding : FragmentLoginFragBinding
    lateinit var navController: NavController
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login_frag,container , false)
        binding.btnLogin.setOnClickListener {
            navController.navigate(
                R.id.action_login_frag_to_otpFragment
            )
        }

        binding.swipeBtn.setOnStateChangeListener {
            if(it){
                startActivity(Intent(context, PoliceDashBoard::class.java))
               (context as Activity).finishAffinity()
            }
            else{
                print("Won't know")
            }
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }
    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}