package com.andrio_kt_dev.billboard.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.andrio_kt_dev.billboard.model.Ad
import com.andrio_kt_dev.billboard.model.DBManager

class FirebaseViewModel:ViewModel() {
    private val dbManager = DBManager()
    val liveAdsData = MutableLiveData<ArrayList<Ad>>()
    fun loadAllAdsFirstPage(filter:String){
        dbManager.getAllAdsFirstPage(filter, object: DBManager.ReadDataCallback{
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsNextPage(time: String, filter: String){
        dbManager.getAllAdsNextPage(time, filter, object: DBManager.ReadDataCallback{
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsFromCat(cat: String, filter:String){
        dbManager.getAllAdsFromCatFirstPage(cat, filter, object: DBManager.ReadDataCallback{
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsFromCatNextPage(cat: String, time: String, filter: String){
        dbManager.getAllAdsFromCatNextPage(cat, time, filter, object: DBManager.ReadDataCallback{
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun adViewed(ad: Ad){
        dbManager.adViewed(ad)
    }

    fun loadMyAds(){
        dbManager.getMyAds(object: DBManager.ReadDataCallback{
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyFavs(){
        dbManager.getMyFavs(object: DBManager.ReadDataCallback{
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun deleteItem(ad:Ad){
        dbManager.deleteAd(ad, object: DBManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                val updatedList = liveAdsData.value
                updatedList?.remove(ad)
                liveAdsData.postValue(updatedList)
            }
        })
    }

    fun onFavClick(ad:Ad){
        dbManager.onFavClick(ad, object : DBManager.FinishWorkListener{
            override fun onFinish(isDone: Boolean) {
                val updatedList = liveAdsData.value
                val pos = updatedList?.indexOf(ad)
                if(pos != -1) {
                    pos?.let{
                        val favCounter = if(ad.isFav) ad.favCounter.toInt() - 1
                        else ad.favCounter.toInt() + 1
                        updatedList[pos] = updatedList[pos].copy(isFav = !ad.isFav, favCounter = favCounter.toString())
                    }
                }
                liveAdsData.postValue(updatedList)
            }
        })
    }

}