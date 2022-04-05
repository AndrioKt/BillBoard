package com.andrio_kt_dev.billboard.dialoghelper

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andrio_kt_dev.billboard.R

class RcDialogSpinnerAdapter(
    private var dialog: AlertDialog,
    private var tvSelection: TextView
) : RecyclerView.Adapter<RcDialogSpinnerAdapter.SpViewHolder>() {
    val mainList = ArrayList<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sp_list_item,parent,false)
        return SpViewHolder(view,tvSelection,dialog)
    }

    override fun onBindViewHolder(holder: SpViewHolder, position: Int) {
        holder.setData(mainList[position])
    }

    override fun getItemCount(): Int {
        return mainList.size
    }


    class SpViewHolder(itemView: View, private var tvSelection: TextView, private var dialog: AlertDialog) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val tvSpItem = itemView.findViewById<TextView>(R.id.tvSpItem)
        fun setData(text:String){
            tvSpItem.text=text
            itemView.setOnClickListener (this)
        }

        override fun onClick(p0: View?) {
            tvSelection.text = tvSpItem.text
            dialog.dismiss()
        }
    }

    fun updateAdapter(list: ArrayList<String>){
        mainList.clear()
        mainList.addAll(list)
        notifyDataSetChanged()
    }
}