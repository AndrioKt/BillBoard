package com.andrio_kt_dev.billboard.frag

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrio_kt_dev.billboard.R
import com.andrio_kt_dev.billboard.databinding.ListImageFragBinding
import com.andrio_kt_dev.billboard.utils.ItemTouchCallback
import kotlinx.coroutines.Job


class ImageListFrag (private val onFragCloseInterface: FragmentCloseInterface, private val newList : ArrayList<String>): Fragment (){
    val adapter = SelectImageRvAdapter()
    private val dragCallback = ItemTouchCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.list_image_frag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bBack = view.findViewById<Button>(R.id.bBack)
        val rcView= view.findViewById<RecyclerView>(R.id.rcViewSelectImage)
        touchHelper.attachToRecyclerView(rcView)
        rcView.layoutManager = LinearLayoutManager(activity)
        rcView.adapter = adapter
        val updateList = ArrayList<SelectImageItem>()
        for (n in 0 until newList.size){
            updateList.add(SelectImageItem(n.toString(),newList[n]))
        }
        adapter.updateAdapter(updateList)
        bBack.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }
    }

    override fun onDetach() {
        super.onDetach()
        onFragCloseInterface.onFragClose(adapter.mainArray)
        Log.d("MyLog", "title 0: ${adapter.mainArray[0].title}")
        Log.d("MyLog", "title 1: ${adapter.mainArray[1].title}")
        Log.d("MyLog", "title 2: ${adapter.mainArray[2].title}")
    }
}