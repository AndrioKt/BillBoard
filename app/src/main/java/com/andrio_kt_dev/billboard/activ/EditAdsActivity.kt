package com.andrio_kt_dev.billboard.activ

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.andrio_kt_dev.billboard.MainActivity
import com.andrio_kt_dev.billboard.R
import com.andrio_kt_dev.billboard.adapters.ImageAdapter
import com.andrio_kt_dev.billboard.model.Ad
import com.andrio_kt_dev.billboard.model.DBManager
import com.andrio_kt_dev.billboard.databinding.ActivityEditAdsBinding
import com.andrio_kt_dev.billboard.dialoghelper.DialogSpinnerHelper
import com.andrio_kt_dev.billboard.frag.FragmentCloseInterface
import com.andrio_kt_dev.billboard.frag.ImageListFrag
import com.andrio_kt_dev.billboard.utils.CityHelper
import com.andrio_kt_dev.billboard.utils.ImagePick


class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface {
    var chooseImagerFrag : ImageListFrag? = null

    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DBManager()
    var editImagePos = 0
    private var isEditState = false
    private var ad:Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        init()
        checkEditState()
    }


    private fun init() {
        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter
    }

    private fun fillViews(ad:Ad) = with (binding){
        tvCountrySelection.text = ad.country
        tvCitySelection.text = ad.city
        edPhoneNumber.setText(ad.phone)
        edIndex.setText(ad.index)
        cbSend.isChecked = ad.send.toBoolean()
        tvCatSelection.text = ad.category
        edName.setText(ad.title)
        edPrice.setText(ad.price)
        edDescription.setText(ad.description)
    }

    private fun isEditState():Boolean{
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun checkEditState(){
        isEditState = isEditState()
        if(isEditState){
            ad = intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad
            if (ad != null) fillViews(ad!!)
        }
    }

    //OnClicks
    fun onClickSelectCountry(view: View){
        val listCountry = CityHelper.getAllCountries(this)
        if(binding.tvCitySelection.text.toString()==getString(R.string.select_city)){
            dialog.showSpinnerDialog(this,listCountry,binding.tvCountrySelection)
        } else {
            binding.tvCitySelection.text = getString(R.string.select_city)
            dialog.showSpinnerDialog(this,listCountry,binding.tvCountrySelection)
        }
    }

    fun onClickSelectCity(view: View){
        val selectedCountry = binding.tvCountrySelection.text.toString()
        if(selectedCountry != getString(R.string.select_country)) {
            val listCity = CityHelper.getAllCities(selectedCountry, this)
            dialog.showSpinnerDialog(this, listCity,binding.tvCitySelection)
        } else Toast.makeText(this, getString(R.string.country_not_selected),Toast.LENGTH_LONG ).show()
    }


    fun onClickSelectImg(view: View){
        if(imageAdapter.mainArray.size == 0 ){
            ImagePick.multiImageLauncher(this, 3)
        } else {
            openChooseImageFrag(null)
            chooseImagerFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scroolViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImagerFrag = null
    }

    fun openChooseImageFrag(newList:ArrayList<Uri>?){
        chooseImagerFrag = ImageListFrag(this)
        if(newList != null) chooseImagerFrag?.resizeSelectedImages(newList, true, this)
        binding.scroolViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImagerFrag!!)
            .commit()
    }

    fun onClickSelectCategory(view: View){
            val listCategories = resources.getStringArray(R.array.categories).toMutableList() as ArrayList<String>
            dialog.showSpinnerDialog(this, listCategories,binding.tvCatSelection)
    }

    fun onClickPublish(view:View){
        val adTemp = fillAd()
        if(isEditState) {
            dbManager.publishAd(adTemp.copy(key = ad?.key), onPublishFinish())

        } else dbManager.publishAd(adTemp, onPublishFinish())
    }

    private fun onPublishFinish() : DBManager.FinishWorkListener{
        return object :DBManager.FinishWorkListener{
            override fun onFinish() {
                finish()
            }
        }
    }

    private fun fillAd():Ad{
        val ad: Ad
        binding.apply {
            ad = Ad(
                tvCountrySelection.text.toString(),
                tvCitySelection.text.toString(),
                edPhoneNumber.text.toString(),
                edIndex.text.toString(),
                cbSend.isChecked.toString(),
                tvCatSelection.text.toString(),
                edName.text.toString(),
                edPrice.text.toString(),
                edDescription.text.toString(),
                dbManager.db.push().key,
                dbManager.auth.uid,
                "0"
                )
        }
        return ad
    }
}
