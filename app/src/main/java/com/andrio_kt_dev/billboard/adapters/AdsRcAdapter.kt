package com.andrio_kt_dev.billboard.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.andrio_kt_dev.billboard.MainActivity
import com.andrio_kt_dev.billboard.R
import com.andrio_kt_dev.billboard.activ.DescriptionActivity
import com.andrio_kt_dev.billboard.activ.EditAdsActivity
import com.andrio_kt_dev.billboard.model.Ad
import com.andrio_kt_dev.billboard.databinding.AdListItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class AdsRcAdapter(val act:MainActivity): RecyclerView.Adapter<AdsRcAdapter.AdsViewHolder>() {
    val adArray = ArrayList<Ad>()
    class AdsViewHolder(val binding: AdListItemBinding,val act:MainActivity) : RecyclerView.ViewHolder(binding.root) {

        fun setData(ad: Ad) {
            binding.apply {
                tvTitle.text = ad.title
                tvDescr.text = ad.description
                tvPrice.text = ad.price
                tvViewCount.text = ad.viewsCounter
                tvFavCount.text = ad.favCounter
                isFav(ad)
                showOwnerPanel(checkOwner(ad))
                onClicks(ad)
                Picasso.get().load(ad.mainImage).into(imMain)
            }
        }
            private fun isFav(ad: Ad){
                if(ad.isFav)  binding.ibFav.setImageResource(R.drawable.ic_fav_presed)
                else  binding.ibFav.setImageResource(R.drawable.ic_fav_unpresed)
            }

        private fun onClicks(ad: Ad){
            binding.ibFav.setOnClickListener {
                if(act.myAuth.currentUser?.isAnonymous == false) act.onFavClicked(ad)
            }
            itemView.setOnClickListener {
                act.onADViewed(ad)
            }
            binding.ibEdit.setOnClickListener(onClickEdit(ad))
            binding.ibDel.setOnClickListener {
                act.onDeleteItem(ad)
            }
        }

        private fun onClickEdit(ad:Ad) :View.OnClickListener{
            return View.OnClickListener {
                val editIntent = Intent(act,EditAdsActivity::class.java).apply {
                    putExtra(MainActivity.EDIT_STATE, true)
                    putExtra(MainActivity.ADS_DATA, ad)
                }
                act.startActivity(editIntent)
            }
        }

        private fun checkOwner(ad:Ad):Boolean{
            return ad.uid == act.myAuth.uid
        }

        private fun showOwnerPanel(isOwner:Boolean){
            if(isOwner) binding.ownerPanel.visibility = View.VISIBLE
            else binding.ownerPanel.visibility = View.GONE
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdsViewHolder {
        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return AdsViewHolder(binding,act)
    }

    override fun onBindViewHolder(holder: AdsViewHolder, position: Int) {
        holder.setData(adArray[position])
    }

    override fun getItemCount(): Int {
        return adArray.size
    }
    fun updateAdapter(newList:List<Ad>){
        val diffResult = DiffUtil.calculateDiff(DifUtilHelper(adArray,newList))
        diffResult.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(newList)
    }
    interface Listener{
        fun onDeleteItem(ad:Ad)
        fun onADViewed(ad:Ad)
        fun onFavClicked(ad:Ad)
    }
}