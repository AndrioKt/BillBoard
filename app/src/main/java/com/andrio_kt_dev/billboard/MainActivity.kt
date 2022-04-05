package com.andrio_kt_dev.billboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.andrio_kt_dev.billboard.activ.EditAdsActivity
import com.andrio_kt_dev.billboard.databinding.ActivityMainBinding
import com.andrio_kt_dev.billboard.dialoghelper.DialogConst
import com.andrio_kt_dev.billboard.dialoghelper.DialogHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val myAuth = FirebaseAuth.getInstance()
    lateinit var launcher: ActivityResultLauncher<Intent>

    private lateinit var tvAccount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        init()
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(myAuth.currentUser)
    }

    private fun init(){
            setSupportActionBar(binding.mainContent.toolbar)
            val toggle = ActionBarDrawerToggle(this,binding.drawerLayout, binding.mainContent.toolbar,R.string.open_menu, R.string.close_menu)
            binding.drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
            binding.navView.setNavigationItemSelectedListener(this)
            tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountMail)
            launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try{
                    val account = task.getResult(ApiException::class.java)
                    if(account != null){
                        dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                    }
                }catch (e:ApiException){
                    Log.d("MyLog", "Api error: ${e.message}")
                }
            }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.id_my_ads ->{

            }
            R.id.id_cars ->{

            }
            R.id.id_pc ->{

            }
            R.id.id_smart ->{

            }
            R.id.id_home_appl ->{

            }
            R.id.id_sign_in ->{
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
            }
            R.id.id_sign_up ->{
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
            }
            R.id.id_sing_out ->{
                uiUpdate(null)
                myAuth.signOut()
                dialogHelper.accHelper.signOutGoogle()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun uiUpdate(user: FirebaseUser?){
        tvAccount.text = if(user == null){
            resources.getString(R.string.not_reg)
        } else {
            user.email
        }
    }

    fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(this.getString(R.string.default_web_client_id)).requestEmail().build()
        return GoogleSignIn.getClient(this,gso)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.new_ads) {
            val intent = Intent(this,EditAdsActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}