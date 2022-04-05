package com.andrio_kt_dev.billboard.utils

import androidx.appcompat.app.AppCompatActivity
import com.fxn.pix.Options
import com.fxn.pix.Pix


object ImagePick {
    const val REQUEST_CODE_GET_IMAGES = 999
    fun getImages(context: AppCompatActivity,imageCounter:Int){
        val options: Options = Options.init()
            .setRequestCode(REQUEST_CODE_GET_IMAGES) //Request code for activity results
            .setCount(3) //Number of images to restict selection count
            .setFrontfacing(false) //Front Facing camera on start
            .setMode(Options.Mode.Picture) //Option to select only pictures or videos or both
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT) //Orientaion
            .setPath("/pix/images") //Custom Path For media Storage

        Pix.start(context, options);
    }
}
