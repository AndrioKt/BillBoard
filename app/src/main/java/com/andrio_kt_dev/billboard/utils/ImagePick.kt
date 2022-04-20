package com.andrio_kt_dev.billboard.utils

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.andrio_kt_dev.billboard.activ.EditAdsActivity
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object ImagePick {
    const val REQUEST_CODE_GET_SINGLE_IMAGE = 998
    const val MAX_IMAGE_COUNT = 3
    private fun getOptions(imageCounter: Int): Options {
        val options: Options = Options.init()
            .setCount(imageCounter) //Number of images to restict selection count
            .setFrontfacing(false) //Front Facing camera on start
            .setMode(Options.Mode.Picture) //Option to select only pictures or videos or both
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT) //Orientaion
            .setPath("/pix/images") //Custom Path For media Storage
        return options
    }

    fun imageLauncher(
        edAct: EditAdsActivity,
        launcher: ActivityResultLauncher<Intent>,
        imageCounter: Int
    ) {
        PermUtil.checkForCamaraWritePermissions(edAct) {
            val intent = Intent(edAct, Pix::class.java).apply {
                putExtra("options", getOptions(imageCounter))
            }
            launcher.launch(intent)
        }
    }

    fun getLauncherForMultiImages(edAct: EditAdsActivity): ActivityResultLauncher<Intent> {
        return edAct.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                if (result.data != null) {
                    val returnValues = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                    if (returnValues?.size!! > 1 && edAct.chooseImagerFrag == null) {
                        edAct.openChooseImageFrag(returnValues)
                    } else if (edAct.chooseImagerFrag != null) {
                        edAct.chooseImagerFrag?.updateAdapter(returnValues)
                    } else if (returnValues.size == 1 && edAct.chooseImagerFrag == null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            edAct.binding.pbLoad.visibility = View.VISIBLE
                            val bitmapArray =
                                ImageManager.imageResize(returnValues) as ArrayList<Bitmap>
                            edAct.binding.pbLoad.visibility = View.GONE
                            edAct.imageAdapter.update(bitmapArray)
                        }
                    }
                }
            }
        }
    }

    fun getLauncherForSingleImage(edAct: EditAdsActivity): ActivityResultLauncher<Intent> {
        return edAct.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                if (result.data != null) {
                    val uris = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                    edAct.chooseImagerFrag?.setSingleImage(uris?.get(0)!!, edAct.editImagePos)
                }
            }
        }
    }
}
