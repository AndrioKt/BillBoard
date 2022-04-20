package com.andrio_kt_dev.billboard.activ

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.andrio_kt_dev.billboard.R
import com.andrio_kt_dev.billboard.adapters.ImageAdapter
import com.andrio_kt_dev.billboard.data.Ad
import com.andrio_kt_dev.billboard.database.DBManager
import com.andrio_kt_dev.billboard.databinding.ActivityEditAdsBinding
import com.andrio_kt_dev.billboard.dialoghelper.DialogSpinnerHelper
import com.andrio_kt_dev.billboard.frag.FragmentCloseInterface
import com.andrio_kt_dev.billboard.frag.ImageListFrag
import com.andrio_kt_dev.billboard.utils.CityHelper
import com.andrio_kt_dev.billboard.utils.ImagePick
import com.fxn.utility.PermUtil


class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface {
    var chooseImagerFrag : ImageListFrag? = null

    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    var editImagePos = 0
    var launcherMultiImage:ActivityResultLauncher<Intent>? = null
    var launcherSingleImage:ActivityResultLauncher<Intent>? = null
    private val dbManager = DBManager(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        init()
    }


    private fun init() {
        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter
        launcherMultiImage = ImagePick.getLauncherForMultiImages(this)
        launcherSingleImage = ImagePick.getLauncherForSingleImage(this)
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
        if(imageAdapter.mainArray.size == 0 && launcherMultiImage != null){
            ImagePick.imageLauncher(this,launcherMultiImage!!, 3)
        } else {
            openChooseImageFrag(null)
            chooseImagerFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
            when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   // ImagePick.getImages(this,3,ImagePick.REQUEST_CODE_GET_IMAGES)
                } else {
                    Toast.makeText(
                        this,
                        "Approve permissions to open Pix ImagePicker",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scroolViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImagerFrag = null
    }

    fun openChooseImageFrag(newList:ArrayList<String>?){
        chooseImagerFrag = ImageListFrag(this, newList)
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
        dbManager.publishAd(fillAd())
        finish()
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
                dbManager.auth.uid
                )
        }
        return ad
    }
}
