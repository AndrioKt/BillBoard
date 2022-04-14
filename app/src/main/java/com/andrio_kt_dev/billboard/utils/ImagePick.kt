package com.andrio_kt_dev.billboard.utils

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.andrio_kt_dev.billboard.activ.EditAdsActivity
import com.fxn.pix.Options
import com.fxn.pix.Pix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object ImagePick {
    const val REQUEST_CODE_GET_IMAGES = 999
    const val REQUEST_CODE_GET_SINGLE_IMAGE = 998
    const val MAX_IMAGE_COUNT = 3
    fun getImages(context: AppCompatActivity,imageCounter:Int, requestCode:Int){
        val options: Options = Options.init()
            .setRequestCode(requestCode) //Request code for activity results
            .setCount(imageCounter) //Number of images to restict selection count
            .setFrontfacing(false) //Front Facing camera on start
            .setMode(Options.Mode.Picture) //Option to select only pictures or videos or both
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT) //Orientaion
            .setPath("/pix/images") //Custom Path For media Storage

        Pix.start(context, options);
    }

    fun showImages(resultCode:Int, requestCode: Int, data: Intent?, edAct:EditAdsActivity){
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == REQUEST_CODE_GET_IMAGES) {
            if (data != null) {
                val returnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                if (returnValues?.size!! > 1 && edAct.chooseImagerFrag == null) {
                    edAct.openChooseImageFrag(returnValues)
                } else if (edAct.chooseImagerFrag != null) {
                    edAct.chooseImagerFrag?.updateAdapter(returnValues)
                } else if (returnValues.size == 1 && edAct.chooseImagerFrag == null) {
                    CoroutineScope(Dispatchers.Main).launch{
                        edAct.binding.pbLoad.visibility = View.VISIBLE
                        val bitmapArray = ImageManager.imageResize(returnValues) as ArrayList<Bitmap>
                        edAct.binding.pbLoad.visibility = View.GONE
                        edAct.imageAdapter.update(bitmapArray)
                    }
                }
            }
        } else if (resultCode == AppCompatActivity.RESULT_OK && requestCode == REQUEST_CODE_GET_SINGLE_IMAGE) {
            if (data != null) {
                val uris = data.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                edAct.chooseImagerFrag?.setSingleImage(uris?.get(0)!!,edAct.editImagePos)
            }
        }
    }
}
