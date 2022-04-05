package com.andrio_kt_dev.billboard.dialoghelper

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrio_kt_dev.billboard.R
import com.andrio_kt_dev.billboard.utils.CityHelper

class DialogSpinnerHelper {
    fun showSpinnerDialog(context: Context, list: ArrayList<String>, tvSelection: TextView){
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_layout,null)
        val dialog = builder.create()
        val adapter = RcDialogSpinnerAdapter(dialog,tvSelection)
        val rcView = view.findViewById<RecyclerView>(R.id.rcSpView)
        val sv = view.findViewById<SearchView>(R.id.svSearch)


        rcView.layoutManager = LinearLayoutManager(context)
        rcView.adapter = adapter
        adapter.updateAdapter(list)

        setSearchView(adapter,list,sv,context)
        dialog.setView(view)
        dialog.show()
    }

    private fun setSearchView(adapter: RcDialogSpinnerAdapter, list: ArrayList<String>, sv: SearchView?, context: Context) {
        sv?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                val tempList = CityHelper.filterListData(list, p0, context)
                adapter.updateAdapter(tempList)
                return true
            }
        })
    }


}