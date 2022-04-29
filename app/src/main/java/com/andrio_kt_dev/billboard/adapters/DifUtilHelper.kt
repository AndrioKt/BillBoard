package com.andrio_kt_dev.billboard.adapters

import androidx.recyclerview.widget.DiffUtil
import com.andrio_kt_dev.billboard.model.Ad

class DifUtilHelper(val oldList:List<Ad>, val newList: List<Ad> ): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].key == newList[newItemPosition].key
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}