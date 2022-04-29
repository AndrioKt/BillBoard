package com.andrio_kt_dev.billboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrio_kt_dev.billboard.accounthelper.AccountHelper
import com.andrio_kt_dev.billboard.activ.EditAdsActivity
import com.andrio_kt_dev.billboard.adapters.AdsRcAdapter
import com.andrio_kt_dev.billboard.databinding.ActivityMainBinding
import com.andrio_kt_dev.billboard.dialoghelper.DialogConst
import com.andrio_kt_dev.billboard.dialoghelper.DialogHelper
import com.andrio_kt_dev.billboard.model.Ad
import com.andrio_kt_dev.billboard.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AdsRcAdapter.Listener {
    lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val myAuth = Firebase.auth
    lateinit var launcher: ActivityResultLauncher<Intent>
    val adapter = AdsRcAdapter(this)
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private lateinit var tvAccount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        init()
        initRecyclerView()
        initViewModel()
        firebaseViewModel.loadAllAds()
        bottomMenuOnClick()
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(myAuth.currentUser)
    }

    private fun initViewModel(){
        firebaseViewModel.liveAdsData.observe(this) {
            adapter.updateAdapter(it)
            binding.mainContent.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
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

    private fun bottomMenuOnClick() = with(binding){
        mainContent.bNavView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.id_new_ad ->  {
                    val intent = Intent(this@MainActivity,EditAdsActivity::class.java)
                    startActivity(intent)
                }
                R.id.id_my_ads -> {
                    firebaseViewModel.loadMyAds()
                    mainContent.toolbar.title = getString(R.string.ad_my_ads)
                }
                R.id.id_favs -> {
                    firebaseViewModel.loadMyFavs()
                }
                R.id.id_home -> {
                    firebaseViewModel.loadAllAds()
                    mainContent.toolbar.title = getString(R.string.general)
                }
            }
            true
        }
    }

    private fun initRecyclerView(){
         binding.apply {
             mainContent.rcAdView.layoutManager = LinearLayoutManager(this@MainActivity)
             mainContent.rcAdView.adapter = adapter
         }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.id_my_ads ->{
                firebaseViewModel.loadMyAds()
                binding.mainContent.toolbar.title = getString(R.string.ad_my_ads)
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
                if (myAuth.currentUser?.isAnonymous == true){
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                uiUpdate(null)
                myAuth.signOut()
                dialogHelper.accHelper.signOutGoogle()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun uiUpdate(user: FirebaseUser?){
        if(user == null){
            dialogHelper.accHelper.signInAnon(object:AccountHelper.Listener{
                override fun onComplete() {
                    tvAccount.setText(R.string.guest)
                }
            })
        } else if(user.isAnonymous){
            tvAccount.setText(R.string.guest)
        } else if (user.isAnonymous == false) {
            tvAccount.text = user.email
        }
    }

    fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(this.getString(R.string.default_web_client_id)).requestEmail().build()
        return GoogleSignIn.getClient(this,gso)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    companion object{
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
    }

    override fun onDeleteItem(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onADViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)
    }

    override fun onFavClicked(ad: Ad) {
        firebaseViewModel.onFavClick(ad)
    }
}