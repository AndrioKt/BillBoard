package com.andrio_kt_dev.billboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andrio_kt_dev.billboard.data.Ad
import com.andrio_kt_dev.billboard.databinding.AdListItemBinding
import com.google.firebase.auth.FirebaseAuth

class AdsRcAdapter(val auth:FirebaseAuth): RecyclerView.Adapter<AdsRcAdapter.AdsViewHolder>() {
    val adArray = ArrayList<Ad>()
    class AdsViewHolder(val binding: AdListItemBinding,val auth:FirebaseAuth) : RecyclerView.ViewHolder(binding.root) {

        fun setData(ad: Ad){
            binding.apply {
                tvTitle.text = ad.title
                tvDescr.text = ad.description
                tvPrice.text = ad.price
            }
            showOwnerPanel(checkOwner(ad))
        }

        private fun checkOwner(ad:Ad):Boolean{
            return ad.uid == auth.uid
        }

        private fun showOwnerPanel(isOwner:Boolean){
            if(isOwner) binding.ownerPanel.visibility = View.VISIBLE
            else binding.ownerPanel.visibility = View.GONE
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdsViewHolder {
        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return AdsViewHolder(binding,auth)
    }

    override fun onBindViewHolder(holder: AdsViewHolder, position: Int) {
        holder.setData(adArray[position])
    }

    override fun getItemCount(): Int {
        return adArray.size
    }
    fun updateAdapter(newList:List<Ad>){
        adArray.clear()
        adArray.addAll(newList)
        notifyDataSetChanged()
    }
}