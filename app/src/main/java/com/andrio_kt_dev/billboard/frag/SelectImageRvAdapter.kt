package com.andrio_kt_dev.billboard.frag

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andrio_kt_dev.billboard.R
import com.andrio_kt_dev.billboard.activ.EditAdsActivity
import com.andrio_kt_dev.billboard.databinding.SelectImageFragItemBinding
import com.andrio_kt_dev.billboard.utils.AdapterCallback
import com.andrio_kt_dev.billboard.utils.ImageManager
import com.andrio_kt_dev.billboard.utils.ImagePick
import com.andrio_kt_dev.billboard.utils.ItemTouchCallback

class SelectImageRvAdapter(val adapterCallback: AdapterCallback): RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(), ItemTouchCallback.ItemTouchAdapter {
    val mainArray = ArrayList<Bitmap>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = SelectImageFragItemBinding.inflate( LayoutInflater.from(parent.context),parent,false)
        return ImageHolder(view,parent.context,this)
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
        mainArray[startPos] = targetItem
        notifyItemMoved(startPos,targetPos)
    }

    override fun onClear() {
        notifyDataSetChanged()
    }

    class ImageHolder(private val binding:SelectImageFragItemBinding,val context: Context,val adapter: SelectImageRvAdapter) :RecyclerView.ViewHolder(binding.root){

        fun setData(bitmap: Bitmap){

            binding.imEditImage.setOnClickListener {
                ImagePick.singleImageLauncher(context as EditAdsActivity)
                context.editImagePos=adapterPosition
            }
            binding.imDelete.setOnClickListener {
                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                for (n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n)
                adapter.adapterCallback.onItemDelete()
            }

            binding.tvTitle.text = context.resources.getStringArray(R.array.title_array)[adapterPosition]
            binding.imageContent.setImageBitmap(bitmap)
            ImageManager.chooseScaleType(binding.imageContent,bitmap)
        }
    }

    fun updateAdapter(newList:List<Bitmap>,needClear: Boolean){
        if(needClear) mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }



}