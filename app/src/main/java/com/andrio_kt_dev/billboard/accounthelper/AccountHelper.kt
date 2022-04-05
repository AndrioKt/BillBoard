package com.andrio_kt_dev.billboard.accounthelper

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.andrio_kt_dev.billboard.MainActivity
import com.andrio_kt_dev.billboard.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*

class AccountHelper(private val act:MainActivity) {
    private lateinit var signInClient: GoogleSignInClient
    fun signUpWithEmail(email:String,password:String){
            if(email.isNotEmpty() && password.isNotEmpty()){
            act.myAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                task->
                if(task.isSuccessful){
                    sendEmailVerification(task.result.user!!)
                    act.uiUpdate(task.result.user)
                } else {
                    Log.d("MyLog","Google signIn Error: ${task.exception}")
                    when {
                        (task.exception is FirebaseAuthUserCollisionException) -> {
                            val exception = task.exception as FirebaseAuthUserCollisionException
                            if (exception.errorCode == FirebaseConst.ERROR_EMAIL_ALREADY_IN_USE) {
                                linkEmailToGoogle(email, password)//link email
                            }
                        }
                        (task.exception is FirebaseAuthInvalidCredentialsException) -> {
                            val exception = task.exception as FirebaseAuthInvalidCredentialsException
                            if (exception.errorCode == FirebaseConst.ERROR_INVALID_EMAIL) {
                                Toast.makeText(act, "ERROR_INVALID_EMAIL", Toast.LENGTH_LONG).show()
                            }
                        }
                        (task.exception is FirebaseAuthWeakPasswordException) -> {
                            val exception = task.exception as FirebaseAuthWeakPasswordException
                            Log.d("MyLog","Google signIn Error: ${exception.errorCode}")
                            if (exception.errorCode == FirebaseConst.ERROR_WEAK_PASSWORD) {
                                Toast.makeText(act, "ERROR_WEAK_PASSWORD", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        } else Toast.makeText(act, R.string.error_empty, Toast.LENGTH_LONG).show()
    }

    fun signInWithEmail(email:String,password:String){
        if(email.isNotEmpty() && password.isNotEmpty()){
            act.myAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    task->
                if(task.isSuccessful){
                    act.uiUpdate(task.result.user)
                } else {
                    Log.d("MyLog","Google signIn Error: ${task.exception}")
                    when {
                        (task.exception is FirebaseAuthInvalidCredentialsException) -> {
                            val exception = task.exception as FirebaseAuthInvalidCredentialsException
                            if (exception.errorCode == FirebaseConst.ERROR_INVALID_EMAIL) {
                                Toast.makeText(act, "ERROR_INVALID_EMAIL", Toast.LENGTH_LONG).show()
                            }
                        }
                        (task.exception is FirebaseAuthInvalidCredentialsException) -> {
                            val exception = task.exception as FirebaseAuthInvalidCredentialsException
                            if (exception.errorCode == FirebaseConst.ERROR_WRONG_PASSWORD) {
                                Toast.makeText(act, "ERROR_WRONG_PASSWORD", Toast.LENGTH_LONG).show()
                            }
                        }
                        (task.exception is FirebaseAuthInvalidUserException) -> {
                            val exception = task.exception as FirebaseAuthInvalidUserException
                            Log.d("MyLog","Google signIn Error: ${exception.errorCode}")
                            if (exception.errorCode == FirebaseConst.ERROR_USER_NOT_FOUND) {
                                Toast.makeText(act, "ERROR_USER_NOT_FOUND", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        } else Toast.makeText(act, R.string.error_empty, Toast.LENGTH_LONG).show()
    }

    private fun sendEmailVerification(user:FirebaseUser){
        user.sendEmailVerification().addOnCompleteListener {
            task->
            if (task.isSuccessful){
                Toast.makeText(act, act.resources.getString(R.string.send_verif_email),Toast.LENGTH_LONG).show()
            } else {
                Log.d("MyLog","Google signIn Error: ${task.exception}")
                Toast.makeText(act, act.resources.getString(R.string.send_verif_email_error),Toast.LENGTH_LONG).show()
            }
        }
    }



    fun signInWithGoogle(launcher: ActivityResultLauncher<Intent>) {
        signInClient = act.getSignInClient()
        launcher.launch(signInClient.signInIntent)
    }
    fun signInFirebaseWithGoogle(token:String){
        val credential = GoogleAuthProvider.getCredential(token,null)
        act.myAuth.signInWithCredential(credential).addOnCompleteListener {
            task -> if (task.isSuccessful) {
                Toast.makeText(act, "Sign in done",Toast.LENGTH_LONG).show()
                act.uiUpdate(task.result?.user)
        } else {
            Log.d("MyLog","Google signIn Error: ${task.exception}")
        }
        }
    }

    private fun linkEmailToGoogle(email:String, password:String){
        val credential = EmailAuthProvider.getCredential(email,password)
        if(act.myAuth.currentUser != null) {
            act.myAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener {
                    task ->
                if (task.isSuccessful){
                    Toast.makeText(act, act.resources.getString(R.string.link_done),Toast.LENGTH_LONG).show()
                } else {
                    Log.d("MyLog","Google signIn Error: ${task.exception}")
                    Toast.makeText(act, act.resources.getString(R.string.enter_to_google),Toast.LENGTH_LONG).show()
                }
            }
        } else Toast.makeText(act, "You are not login",Toast.LENGTH_LONG).show()
    }

    fun signOutGoogle() {
        act.getSignInClient().signOut()
    }
}