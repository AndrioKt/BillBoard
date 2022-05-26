package com.andrio_kt_dev.billboard.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.andrio_kt_dev.billboard.MainActivity
import com.andrio_kt_dev.billboard.R
import com.andrio_kt_dev.billboard.activ.EditAdsActivity
import com.andrio_kt_dev.billboard.model.Ad
import com.andrio_kt_dev.billboard.databinding.AdListItemBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdsRcAdapter(val act:MainActivity): RecyclerView.Adapter<AdsRcAdapter.AdsViewHolder>() {
    val adArray = ArrayList<Ad>()
    private var timeFormatter:SimpleDateFormat? = null

    init {
         timeFormatter = SimpleDateFormat("dd.MM.yyyy - hh:mm", Locale.getDefault())
    }
    class AdsViewHolder(val binding: AdListItemBinding,val act:MainActivity,val formatter:SimpleDateFormat) : RecyclerView.ViewHolder(binding.root) {

        fun setData(ad: Ad) {
            binding.apply {
                tvTitle.text = ad.title
                tvDescr.text = ad.description
                tvPrice.text = ad.price
                tvViewCount.text = ad.viewsCounter
                tvFavCount.text = ad.favCounter
                val publishTime = act.getString(R.string.publish_time) + " " + getTimeFromMil(ad.time)
                    tvPublishTime.text = publishTime
                onClicks(ad)
                isFav(ad)
                showOwnerPanel(checkOwner(ad))
                if(ad.mainImage!="empty")Picasso.get().load(ad.mainImage).into(imMain)
                else imMain.setImageResource(R.drawable.ic_deafault_ad_img)
            }
        }

        private fun getTimeFromMil(timeMil:String):String{
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeMil.toLong()
            return formatter.format(calendar.time)
        }

        private fun isFav(ad: Ad) {
            if (ad.isFav) binding.ibFav.setImageResource(R.drawable.ic_fav_presed)
            else binding.ibFav.setImageResource(R.drawable.ic_fav_unpresed)
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
        return AdsViewHolder(binding,act, timeFormatter!!)
    }

    override fun onBindViewHolder(holder: AdsViewHolder, position: Int) {
        holder.setData(adArray[position])
    }

    override fun getItemCount(): Int {
        return adArray.size
    }
    fun updateAdapter(newList:List<Ad>){
        val tempArray = ArrayList<Ad>()
        tempArray.addAll(adArray)
        tempArray.addAll(newList)
        val diffResult = DiffUtil.calculateDiff(DifUtilHelper(adArray,tempArray))
        diffResult.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(tempArray)
    }

    fun updateAdapterWithClear(newList:List<Ad>){
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