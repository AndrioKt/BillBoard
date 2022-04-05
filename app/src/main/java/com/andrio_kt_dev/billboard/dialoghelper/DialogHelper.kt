package com.andrio_kt_dev.billboard.dialoghelper

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.andrio_kt_dev.billboard.MainActivity
import com.andrio_kt_dev.billboard.R
import com.andrio_kt_dev.billboard.accounthelper.AccountHelper
import com.andrio_kt_dev.billboard.databinding.SigninDialogBinding

class DialogHelper(private val act: MainActivity) {

    val accHelper = AccountHelper(act)
    fun createSignDialog(index:Int){
        val builder = AlertDialog.Builder(act)
        val binding = SigninDialogBinding.inflate(act.layoutInflater)
        val dialog = builder.create()
        dialog.setView(binding.root)
        checkDialogState(index,binding)
        binding.btSignUpIn.setOnClickListener { onClickSignUpIn(index,binding,dialog) }
        binding.btForgetP.setOnClickListener { onClickResetPass(binding) }
        binding.btGoogleSignIn.setOnClickListener {
            accHelper.signInWithGoogle(act.launcher)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun onClickResetPass(binding: SigninDialogBinding) {
        if(binding.edSignMail.text.isNotEmpty()){
            act.myAuth.sendPasswordResetEmail(binding.edSignMail.text.toString()).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(act,R.string.send_reset_pass,Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(act,R.string.send_reset_pass_error,Toast.LENGTH_LONG).show()
                }
            }
        } else {
            binding.tvDialogMessage.visibility = View.VISIBLE
        }
    }

    private fun onClickSignUpIn(index: Int, binding: SigninDialogBinding, dialog: AlertDialog?) {
        binding.btSignUpIn.setOnClickListener {
            dialog?.dismiss()
            if(index == DialogConst.SIGN_UP_STATE){
                accHelper.signUpWithEmail(binding.edSignMail.text.toString(),binding.edSignPassword.text.toString())
            } else {
                accHelper.signInWithEmail(binding.edSignMail.text.toString(),binding.edSignPassword.text.toString())
            }
        }

    }

    private fun checkDialogState(index: Int, binding: SigninDialogBinding) {
        if (index == DialogConst.SIGN_UP_STATE){
            binding.tvSignTitle.text = act.resources.getString(R.string.ad_sign_up)
            binding.btSignUpIn.text = act.resources.getString(R.string.sign_up_action)
        } else {
            binding.tvSignTitle.text = act.resources.getString(R.string.ad_sign_in)
            binding.btSignUpIn.text = act.resources.getString(R.string.sign_in_action)
            binding.btForgetP.visibility = View.VISIBLE
        }
    }

}