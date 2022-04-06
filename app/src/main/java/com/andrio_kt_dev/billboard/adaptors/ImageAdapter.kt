package com.andrio_kt_dev.billboard.adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.andrio_kt_dev.billboard.R

class ImageAdapter:RecyclerView.Adapter<ImageAdapter.ImageHolder>() {
    var mainArray = ArrayList<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_adapter_item,parent,false)
        return ImageHolder(view)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }


    class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var imItem :ImageView
        fun setData(uri: String){
            imItem = itemView.findViewById(R.id.imItem)
            imItem.setImageURI(android.net.Uri.parse(uri))
        }
    }
    fun update(newList: ArrayList<String>){
        mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }
}