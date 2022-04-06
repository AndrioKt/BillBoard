package com.andrio_kt_dev.billboard.frag

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrio_kt_dev.billboard.R
import com.andrio_kt_dev.billboard.databinding.ListImageFragBinding
import com.andrio_kt_dev.billboard.utils.ImagePick
import com.andrio_kt_dev.billboard.utils.ItemTouchCallback
import kotlinx.coroutines.Job


class ImageListFrag (private val onFragCloseInterface: FragmentCloseInterface, private val newList : ArrayList<String>): Fragment (){
    lateinit var binding: ListImageFragBinding
    val adapter = SelectImageRvAdapter()
    private val dragCallback = ItemTouchCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ListImageFragBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        touchHelper.attachToRecyclerView(binding.rcViewSelectImage)
        binding.rcViewSelectImage.layoutManager = LinearLayoutManager(activity)
        binding.rcViewSelectImage.adapter = adapter
        adapter.updateAdapter(newList, true)
    }

    override fun onDetach() {
        super.onDetach()
        onFragCloseInterface.onFragClose(adapter.mainArray)
    }

    private fun setUpToolbar(){
        binding.tb.inflateMenu(R.menu.menu_choose_img)
        val deleteItem = binding.tb.menu.findItem(R.id.id_delete_img)
        val addItem = binding.tb.menu.findItem(R.id.id_add_img)

        binding.tb.setNavigationOnClickListener{
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }
        deleteItem.setOnMenuItemClickListener {
            adapter.updateAdapter(ArrayList(), true)
            true
        }
        addItem.setOnMenuItemClickListener {
            val imageCount = ImagePick.MAX_IMAGE_COUNT - adapter.mainArray.size
            ImagePick.getImages(activity as AppCompatActivity,imageCount)
            true
        }
    }

    fun updateAdapter(newList: ArrayList<String>){
        adapter.updateAdapter(newList, false)
    }
}