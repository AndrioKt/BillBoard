package com.andrio_kt_dev.billboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrio_kt_dev.billboard.accounthelper.AccountHelper
import com.andrio_kt_dev.billboard.activ.DescriptionActivity
import com.andrio_kt_dev.billboard.activ.EditAdsActivity
import com.andrio_kt_dev.billboard.activ.FilterActivity
import com.andrio_kt_dev.billboard.adapters.AdsRcAdapter
import com.andrio_kt_dev.billboard.databinding.ActivityMainBinding
import com.andrio_kt_dev.billboard.dialoghelper.DialogConst
import com.andrio_kt_dev.billboard.dialoghelper.DialogHelper
import com.andrio_kt_dev.billboard.model.Ad
import com.andrio_kt_dev.billboard.utils.FilterManager
import com.andrio_kt_dev.billboard.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AdsRcAdapter.Listener {
    lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val myAuth = Firebase.auth
    lateinit var launcher: ActivityResultLauncher<Intent>
    lateinit var filterLauncher: ActivityResultLauncher<Intent>
    val adapter = AdsRcAdapter(this)
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private lateinit var tvAccount: TextView
    private lateinit var imAccount: ImageView
    private var clearUpdate: Boolean = true
    private var currentCat: String? = null
    private var filter: String = "empty"
    private var filterDb: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        init()
        initRecyclerView()
        initViewModel()
        bottomMenuOnClick()
        scrollListener()
        onActivityResultFilter()
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home
    }

    private fun onActivityResultFilter(){
        filterLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == RESULT_OK){
                filter = it.data?.getStringExtra(FilterActivity.FILTER_KEY)!!
                filterDb = FilterManager.getFilter(filter)
            } else if (it.resultCode == RESULT_CANCELED){
                filterDb = ""
                filter = "empty"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(myAuth.currentUser)
    }

    private fun initViewModel(){
        firebaseViewModel.liveAdsData.observe(this) {
            val list = getAdsByCat(it)
            if (!clearUpdate) adapter.updateAdapter(list)
            else adapter.updateAdapterWithClear(list)
            binding.mainContent.tvEmpty.visibility =
                if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    private fun getAdsByCat(list: ArrayList<Ad>):ArrayList<Ad>{
        val tempList = ArrayList<Ad>()
        tempList.addAll(list)
        if(currentCat != getString(R.string.general)) {
            tempList.clear()
            list.forEach{
                if(currentCat == it.category) tempList.add(it)
            }
        }
        tempList.reverse()
        return tempList
    }

    private fun init(){
            currentCat = getString(R.string.general)
            setSupportActionBar(binding.mainContent.toolbar)
            val toggle = ActionBarDrawerToggle(this,binding.drawerLayout, binding.mainContent.toolbar,R.string.open_menu, R.string.close_menu)
            binding.drawerLayout.addDrawerListener(toggle)
            navViewSettings()
            toggle.syncState()
            binding.navView.setNavigationItemSelectedListener(this)
            tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountMail)
            imAccount = binding.navView.getHeaderView(0).findViewById(R.id.imAccount)

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
            clearUpdate = true
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
                    mainContent.toolbar.title = getString(R.string.favorites)
                }
                R.id.id_home -> {
                    currentCat = getString(R.string.general)
                    firebaseViewModel.loadAllAdsFirstPage(filterDb)
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
        clearUpdate = true
        when(item.itemId){
            R.id.id_my_ads ->{
                firebaseViewModel.loadMyAds()
                binding.mainContent.toolbar.title = getString(R.string.ad_my_ads)
            }
            R.id.id_favorite ->{
                firebaseViewModel.loadMyFavs()
                binding.mainContent.toolbar.title = getString(R.string.favorites)
            }
            R.id.id_cars ->{
                getAdsFromCat(CAT_CAR)
                binding.mainContent.toolbar.title = getString(R.string.ad_car)
            }
            R.id.id_pc ->{
                getAdsFromCat(CAT_PC)
                binding.mainContent.toolbar.title = getString(R.string.ad_pc)
            }
            R.id.id_smart ->{
                getAdsFromCat(CAT_SMART)
                binding.mainContent.toolbar.title = getString(R.string.ad_smartphones)
            }
            R.id.id_home_appl ->{
                getAdsFromCat(CAT_APL)
                binding.mainContent.toolbar.title = getString(R.string.ad_home_appliances)
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

    private fun getAdsFromCat(cat:String){
        currentCat = cat
        firebaseViewModel.loadAllAdsFromCat(cat, filterDb)
    }

    fun uiUpdate(user: FirebaseUser?){
        if(user == null){
            dialogHelper.accHelper.signInAnon(object:AccountHelper.Listener{
                override fun onComplete() {
                    tvAccount.setText(R.string.guest)
                    imAccount.setImageResource(R.drawable.ic_default_avatar)
                }
            })
        } else if(user.isAnonymous){
            tvAccount.setText(R.string.guest)
            imAccount.setImageResource(R.drawable.ic_default_avatar)
        } else if (!user.isAnonymous) {
            tvAccount.text = user.email
            Picasso.get().load(user.photoUrl).into(imAccount)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.filter) {
            val intent = Intent(this@MainActivity, FilterActivity::class.java).apply {
                putExtra(FilterActivity.FILTER_KEY, filter)
            }
                filterLauncher.launch(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDeleteItem(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onADViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)
        val intent = Intent(this, DescriptionActivity::class.java)
        intent.putExtra("AD", ad)
        startActivity(intent)
    }

    override fun onFavClicked(ad: Ad) {
        firebaseViewModel.onFavClick(ad)
    }

    private fun navViewSettings() = with(binding) {
        val menu = navView.menu
        val adCategories = menu.findItem(R.id.adCategories)
        val accCategories = menu.findItem(R.id.accCategories)
        val spanAdsCat = SpannableString(adCategories.title)
        val spanAccCat = SpannableString(accCategories.title)
        spanAdsCat.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity,R.color.Text_color)),0,adCategories.title.length,0)
        spanAccCat.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity,R.color.Text_color)),0,accCategories.title.length,0)
        adCategories.title = spanAdsCat
        accCategories.title = spanAccCat
    }

    private fun scrollListener() = with(binding.mainContent){
        rcAdView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(!recyclerView.canScrollVertically(SCROLL_DOWN) && newState == RecyclerView.SCROLL_STATE_IDLE){
                    clearUpdate = false
                    val adsList = firebaseViewModel.liveAdsData.value!!
                    if(adsList.isNotEmpty()){
                        getAdsFromCat(adsList)
                    }
                }
            }
        })
    }

    private fun getAdsFromCat(adsList: ArrayList<Ad>){
        adsList[0].let {
            if (currentCat == getString(R.string.general)) {
                firebaseViewModel.loadAllAdsNextPage(it.time, filterDb)
            } else {
                    firebaseViewModel.loadAllAdsFromCatNextPage(it.category!!, it.time, filterDb)
            }
        }
    }

    companion object{
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        const val SCROLL_DOWN = 1

        const val CAT_CAR = "Автомобили"
        const val CAT_PC = "Компьютеры"
        const val CAT_SMART = "Смартфоны"
        const val CAT_APL = "Бытовая техника"

        const val CAT_MY_ADS = "Мои объявления"
        const val CAT_FAV = "Избранные"
       // const val CAT_APL = "Бытовая техника"
    }
}