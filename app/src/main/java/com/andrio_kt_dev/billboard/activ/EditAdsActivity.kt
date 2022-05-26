package com.andrio_kt_dev.billboard.activ

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
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
import com.andrio_kt_dev.billboard.utils.ImageManager
import com.andrio_kt_dev.billboard.utils.ImagePick
import com.google.android.gms.tasks.OnCompleteListener
import java.io.ByteArrayOutputStream


class EditAdsActivity : AppCompatActivity(), FragmentCloseInterface {
    var chooseImagerFrag : ImageListFrag? = null

    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DBManager()
    var editImagePos = 0
    private var imgIndex = 0
    private var isEditState = false
    private var ad:Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        init()
        checkEditState()
        imageCounter()
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
        updateImgCounter(0)
        ImageManager.fillImageArray(ad, imageAdapter)
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
        updateImgCounter(binding.vpImages.currentItem)
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
        if(isFieldsEmpty()){
            showToast(getString(R.string.fields_fill))
            return
        }
        binding.progressLay.visibility = View.VISIBLE
        ad = fillAd()
        uploadImages()
    }

    private fun onPublishFinish() : DBManager.FinishWorkListener{
        return object :DBManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                binding.progressLay.visibility = View.GONE
                if(isDone) finish()
            }
        }
    }

    private fun isFieldsEmpty():Boolean = with(binding){
        return tvCountrySelection.text.toString() == getString(R.string.select_country)
                || tvCitySelection.text.toString() == getString(R.string.select_city)
                || tvCatSelection.text.toString() == getString(R.string.select_category)
                || edPhoneNumber.text.isEmpty()
                || edPrice.text.isEmpty()
                || edName.text.isEmpty()
    }

    private fun fillAd():Ad{
        val adTemp: Ad
        binding.apply {
            adTemp = Ad(
                tvCountrySelection.text.toString(),
                tvCitySelection.text.toString(),
                edPhoneNumber.text.toString(),
                edIndex.text.toString(),
                cbSend.isChecked.toString(),
                tvCatSelection.text.toString(),
                edName.text.toString(),
                edPrice.text.toString(),
                edDescription.text.toString(),
                edEmail.text.toString(),
                ad?.mainImage ?: "empty",
                ad?.image2 ?: "empty",
                ad?.image3 ?: "empty",
                ad?.key ?: dbManager.db.push().key,
                dbManager.auth.uid,
                ad?.time ?: System.currentTimeMillis().toString(),
                "0"
                )
        }
        return adTemp
    }

    private fun uploadImages() {
        if (imgIndex == ImagePick.MAX_IMAGE_COUNT) {
            dbManager.publishAd(ad!!, onPublishFinish())
            return
        }
        val oldUrl = getURL()
        if (imageAdapter.mainArray.size > imgIndex) {
            val byteArray = imageToByteArray(imageAdapter.mainArray[imgIndex])
            if (oldUrl.startsWith("http")) {
                updateImage(byteArray, oldUrl) {
                    nextImg(it.result.toString())
                }
            } else {
                loadSingleImage(byteArray) {
                    nextImg(it.result.toString())
                }
            }
        } else {
            if (oldUrl.startsWith("http")) {
                deleteImgByUrl(oldUrl) {
                    nextImg("empty")
                }
            } else {
                nextImg("empty")
            }
        }
    }

    private fun nextImg(uri:String){
        setImageUri(uri)
        imgIndex++
        uploadImages()
    }

    private fun setImageUri(uri:String){
        when(imgIndex){
            0->ad = ad?.copy(mainImage = uri)
            1->ad = ad?.copy(image2 = uri)
            2->ad = ad?.copy(image3 = uri)
        }
    }

    private fun imageToByteArray(bitmap: Bitmap): ByteArray{
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream)
        return outStream.toByteArray()
    }

    private fun loadSingleImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>){
        val imStorageRef = dbManager.dbStorage.child(dbManager.auth.uid!!).child("image_${System.currentTimeMillis()}")
        val uploadTask = imStorageRef.putBytes(byteArray)
        uploadTask.continueWithTask{
            imStorageRef.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun getURL():String{
        return listOf(ad?.mainImage!!, ad?.image2!!, ad?.image3!!)[imgIndex]
    }

    private fun updateImage(byteArray: ByteArray, url:String, listener: OnCompleteListener<Uri>){
        val imStorageRef = dbManager.dbStorage.storage.getReferenceFromUrl(url)
        val uploadTask = imStorageRef.putBytes(byteArray)
        uploadTask.continueWithTask{
                imStorageRef.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun deleteImgByUrl(oldUrl:String, listener: OnCompleteListener<Void>){
       dbManager.dbStorage.storage.getReferenceFromUrl(oldUrl).delete().addOnCompleteListener(listener)
    }

    private fun imageCounter(){
        binding.vpImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImgCounter(position)
            }
        })
    }

    private fun updateImgCounter(counter:Int){
        var index = 1
        val itemCount = binding.vpImages.adapter?.itemCount
        if(itemCount == 0) index = 0
        val imageCounter = "${counter + index}/$itemCount"
        binding.tvImageCounter.text =  imageCounter
    }
}
