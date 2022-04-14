package com.andrio_kt_dev.billboard.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import com.andrio_kt_dev.billboard.databinding.ProgressDialogLayoutBinding

object ProgressDialog {

    fun createProgressDialog(act: Activity): AlertDialog{
        val builder = AlertDialog.Builder(act)
        val binding = ProgressDialogLayoutBinding.inflate(act.layoutInflater)
        val dialog = builder.create()
        dialog.setView(binding.root)
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }
}