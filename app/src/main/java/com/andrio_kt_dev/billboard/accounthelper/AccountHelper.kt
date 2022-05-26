package com.andrio_kt_dev.billboard.accounthelper

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.andrio_kt_dev.billboard.MainActivity
import com.andrio_kt_dev.billboard.R
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.*
import java.lang.Exception

class AccountHelper(private val act:MainActivity) {
    private lateinit var signInClient: GoogleSignInClient
    fun signUpWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.myAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    act.myAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task1 ->
                            if (task1.isSuccessful) {
                                signUpWithEmailSuccessful(task1.result.user!!)
                            } else {
                                signUpWithEmailException(task1.exception!!, email, password)
                            }
                        }
                }
            }
        }
    }

    private fun signInWithEmailExceptions(exception:Exception, email: String, password: String){
        when {
            (exception is FirebaseAuthInvalidCredentialsException) -> {
                if (exception.errorCode == FirebaseConst.ERROR_INVALID_EMAIL) {
                    Toast.makeText(act, "ERROR_INVALID_EMAIL", Toast.LENGTH_LONG).show()
                }
            }
            (exception is FirebaseAuthInvalidCredentialsException) -> {
                if (exception.errorCode == FirebaseConst.ERROR_WRONG_PASSWORD) {
                    Toast.makeText(act, "ERROR_WRONG_PASSWORD", Toast.LENGTH_LONG).show()
                }
            }
            (exception is FirebaseAuthInvalidUserException) -> {
                Log.d("MyLog","Google signIn Error: ${exception.errorCode}")
                if (exception.errorCode == FirebaseConst.ERROR_USER_NOT_FOUND) {
                    Toast.makeText(act, "ERROR_USER_NOT_FOUND", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun signUpWithEmailSuccessful(user:FirebaseUser){
        sendEmailVerification(user)
        act.uiUpdate(user)
    }

    private fun signUpWithEmailException(exception:Exception, email: String, password: String){
        when {
            (exception is FirebaseAuthUserCollisionException) -> {
                val exception = exception
                if (exception.errorCode == FirebaseConst.ERROR_EMAIL_ALREADY_IN_USE) {
                    linkEmailToGoogle(email, password)//link email
                }
            }
            (exception is FirebaseAuthInvalidCredentialsException) -> {
                val exception = exception
                if (exception.errorCode == FirebaseConst.ERROR_INVALID_EMAIL) {
                    Toast.makeText(act, "ERROR_INVALID_EMAIL", Toast.LENGTH_LONG).show()
                }
            }
            (exception is FirebaseAuthWeakPasswordException) -> {
                if (exception.errorCode == FirebaseConst.ERROR_WEAK_PASSWORD) {
                    Toast.makeText(act, "ERROR_WEAK_PASSWORD", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.myAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    act.myAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                act.uiUpdate(task.result.user)
                            } else {
                                signInWithEmailExceptions(task.exception!!, email, password)
                            }
                        }
                }
            }
        }
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
        act.myAuth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                act.myAuth.signInWithCredential(credential).addOnCompleteListener {
                        task2 -> if (task2.isSuccessful) {
                    Toast.makeText(act, "Sign in done",Toast.LENGTH_LONG).show()
                    act.uiUpdate(task2.result?.user)
                } else {
                    Toast.makeText(act, "Google signIn Error: ${task.exception}",Toast.LENGTH_LONG).show()
                }
                }
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

    fun signInAnon(listener: Listener){
        act.myAuth.signInAnonymously().addOnCompleteListener {
            task ->
            if(task.isSuccessful) {
                listener.onComplete()
                Toast.makeText(act,"Вы вошли как гость", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(act,"Не удалось войти как гость", Toast.LENGTH_LONG).show()
            }
        }
    }

    interface Listener{
        fun onComplete()
    }
}