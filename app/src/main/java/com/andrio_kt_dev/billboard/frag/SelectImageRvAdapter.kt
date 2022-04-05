package com.andrio_kt_dev.billboard.frag

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andrio_kt_dev.billboard.R
import com.andrio_kt_dev.billboard.utils.ItemTouchCallback

class SelectImageRvAdapter: RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(), ItemTouchCallback.ItemTouchAdapter {
    val mainArray = ArrayList<SelectImageItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.select_image_frag_item,parent,false)
        return ImageHolder(view)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
       return mainArray.size
    }

    override fun onMove(startPos: Int, targetPos: Int) {
        val targetItem = mainArray[targetPos]
        mainArray[targetPos] = mainArray[startPos]
        val startTitle = mainArray[targetPos].title
        mainArray[targetPos].title = targetItem.title
        mainArray[startPos] = targetItem
        mainArray[startPos].title = startTitle
        notifyItemMoved(startPos,targetPos)
    }

    override fun onClear() {
        notifyDataSetChanged()
    }

    class ImageHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        private lateinit var tvTitle: TextView
        lateinit var image: ImageView
        fun setData(item: SelectImageItem){
            tvTitle = itemView.findViewById(R.id.tvTitle)
            image = itemView.findViewById(R.id.imageContent)
            tvTitle.text = item.title
            image.setImageURI(Uri.parse(item.imageUri))
        }
    }

    fun updateAdapter(newList:List<SelectImageItem>){
        mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }



}