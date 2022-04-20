package com.andrio_kt_dev.billboard.frag

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrio_kt_dev.billboard.R
import com.andrio_kt_dev.billboard.activ.EditAdsActivity
import com.andrio_kt_dev.billboard.databinding.ListImageFragBinding
import com.andrio_kt_dev.billboard.dialoghelper.ProgressDialog
import com.andrio_kt_dev.billboard.utils.AdapterCallback
import com.andrio_kt_dev.billboard.utils.ImageManager
import com.andrio_kt_dev.billboard.utils.ImagePick
import com.andrio_kt_dev.billboard.utils.ItemTouchCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class ImageListFrag (private val onFragCloseInterface: FragmentCloseInterface, private val newList : ArrayList<String>?): BaseAdsFrag(), AdapterCallback{
    val adapter = SelectImageRvAdapter(this)
    private val dragCallback = ItemTouchCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    private var job:Job? = null
    private var addItem: MenuItem? = null
    lateinit var binding: ListImageFragBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ListImageFragBinding.inflate(layoutInflater)
        adView = binding.adView
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        touchHelper.attachToRecyclerView(binding.rcViewSelectImage)
        binding.rcViewSelectImage.layoutManager = LinearLayoutManager(activity)
        binding.rcViewSelectImage.adapter = adapter
        if(newList != null){
            resizeSelectedImages(newList,true)
        }
    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>){
        adapter.updateAdapter(bitmapList, true)
    }

    override fun onDetach() {
        super.onDetach()
        onFragCloseInterface.onFragClose(adapter.mainArray)
        job?.cancel()
    }

    override fun onClose() {
        super.onClose()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

    private fun setUpToolbar(){
        binding.tb.inflateMenu(R.menu.menu_choose_img)
        val deleteItem = binding.tb.menu.findItem(R.id.id_delete_img)
        addItem = binding.tb.menu.findItem(R.id.id_add_img)

        binding.tb.setNavigationOnClickListener{
            showInterAd()
        }

        deleteItem.setOnMenuItemClickListener {
            adapter.updateAdapter(ArrayList(), true)
            addItem?.isVisible = true
            true
        }
        addItem?.setOnMenuItemClickListener {
            val imageCount = ImagePick.MAX_IMAGE_COUNT - adapter.mainArray.size
            ImagePick.imageLauncher(activity as EditAdsActivity, (activity as EditAdsActivity).launcherMultiImage!! ,imageCount )
            true
        }
    }

    fun updateAdapter(newList: ArrayList<String>){
        resizeSelectedImages(newList,false)
    }

    fun setSingleImage (uri:String, pos:Int){
        val pBar = binding.rcViewSelectImage[pos].findViewById<ProgressBar>(R.id.pBar)
        job = CoroutineScope(Dispatchers.Main).launch {
            pBar.visibility = View.VISIBLE
            val bitmapList = ImageManager.imageResize(listOf(uri))
            pBar.visibility = View.GONE
            adapter.mainArray[pos] = bitmapList[0]
            adapter.notifyItemChanged(pos)
        }
    }

    private fun resizeSelectedImages(newList:ArrayList<String>, needClear:Boolean){
        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createProgressDialog(activity as Activity)
            val bitmapList = ImageManager.imageResize(newList)
            dialog.dismiss()
            adapter.updateAdapter(bitmapList, needClear)
            if(adapter.mainArray.size > 2) addItem?.isVisible = false

        }
    }

    override fun onItemDelete() {
        addItem?.isVisible = true
    }
}