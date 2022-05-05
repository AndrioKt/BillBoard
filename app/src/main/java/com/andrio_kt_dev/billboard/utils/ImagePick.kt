package com.andrio_kt_dev.billboard.utils

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.andrio_kt_dev.billboard.R
import com.andrio_kt_dev.billboard.activ.EditAdsActivity
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object ImagePick {
    const val MAX_IMAGE_COUNT = 3
    private fun getOptions(imageCounter: Int): Options {
        val options: Options = Options().apply {
            count = imageCounter
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
        }
        return options
    }

    fun multiImageLauncher(edAct: EditAdsActivity, imageCounter: Int) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    getMultiImages(edAct, result.data)
                }
            }
        }
    }
    fun addImages(edAct: EditAdsActivity, imageCounter: Int) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(edAct)
                    edAct.chooseImagerFrag?.updateAdapter(result.data as ArrayList<Uri>, edAct)
                }
            }
        }
    }
    fun singleImageLauncher(edAct: EditAdsActivity) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(1)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(edAct)
                    getSingleImage(edAct,result.data[0])
                }
            }
        }
    }

    private fun openChooseImageFrag(edAct: EditAdsActivity){
        edAct.supportFragmentManager.beginTransaction().replace(R.id.place_holder, edAct.chooseImagerFrag!!).commit()
    }

    private fun closePixFrag(edAct: EditAdsActivity){
        val fList = edAct.supportFragmentManager.fragments
        fList.forEach {
            if (it.isVisible) edAct.supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    fun getMultiImages(edAct: EditAdsActivity, uris: List<Uri>) {
        if (uris.size > 1 && edAct.chooseImagerFrag == null) {
            edAct.openChooseImageFrag(uris as ArrayList<Uri>)
        } else if (edAct.chooseImagerFrag != null) {
            edAct.chooseImagerFrag?.updateAdapter(uris as ArrayList<Uri>, edAct)
        } else if (uris.size == 1 && edAct.chooseImagerFrag == null) {
            CoroutineScope(Dispatchers.Main).launch {
                edAct.binding.pbLoad.visibility = View.VISIBLE
                val bitmapArray =
                    ImageManager.imageResize(uris as ArrayList<Uri>,edAct) as ArrayList<Bitmap>
                edAct.binding.pbLoad.visibility = View.GONE
                edAct.imageAdapter.update(bitmapArray)
                closePixFrag(edAct)
            }
        }
    }

    fun getSingleImage(edAct: EditAdsActivity, uri:Uri) {
        edAct.chooseImagerFrag?.setSingleImage(uri, edAct.editImagePos)
    }
}
