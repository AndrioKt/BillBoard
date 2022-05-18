package com.andrio_kt_dev.billboard.model

import com.andrio_kt_dev.billboard.utils.FilterManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class DBManager {
    val db = Firebase.database.getReference(MAIN_NODE)
    val dbStorage = Firebase.storage.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun publishAd(ad: Ad,finishListener:FinishWorkListener){
       if(auth.uid != null) db.child(ad.key ?: "empty")
           .child(auth.uid!!).child(AD_NODE)
           .setValue(ad).addOnCompleteListener {
               if(it.isSuccessful) {
                   val adFilter = FilterManager.createFilter(ad)
                   db.child(ad.key ?: "empty")
                       .child(FILTER_NODE).setValue(adFilter).addOnCompleteListener {
                           finishListener.onFinish(it.isSuccessful)
                       }
               }
           }
       }

    fun adViewed(ad: Ad){
        var counter = ad.viewsCounter.toInt()
        counter++
        if(auth.uid != null) db.child(ad.key ?: "empty")
            .child(INFO_NODE)
            .setValue(InfoItem(counter.toString(), ad.emailCounter, ad.callsCounter))
    }

    fun addToFavs(ad: Ad, listener: FinishWorkListener){
        ad.key?.let {
            auth.uid?.let {
                    uid -> db.child(it).child(FAVS_NODE).child(uid).setValue(uid).addOnCompleteListener {
                     if(it.isSuccessful) listener.onFinish(true)
                 }
            }
        }
    }

    private fun removeFromFavs(ad: Ad, listener: FinishWorkListener){
        ad.key?.let {
            auth.uid?.let {
                    uid -> db.child(it).child(FAVS_NODE).child(uid).removeValue().addOnCompleteListener {
                if(it.isSuccessful) listener.onFinish(true)
            }
            }
        }
    }

    fun onFavClick(ad: Ad, listener: FinishWorkListener){
        if(ad.isFav) removeFromFavs(ad, listener)
        else addToFavs(ad, listener)
    }

    fun getMyAds(readCallback: ReadDataCallback?){
        val query = db.orderByChild(auth.uid + "/ad/uid").equalTo(auth.uid)
        readDBData(query, readCallback)
    }

    fun getMyFavs(readCallback: ReadDataCallback?){
        val query = db.orderByChild("favs/${auth.uid}").equalTo(auth.uid)
        readDBData(query, readCallback)

    }

    fun getAllAdsFirstPage(filter:String, readCallback: ReadDataCallback?){
        val query = if(filter.isEmpty()){
            db.orderByChild(FILTER_TIME_PATH).limitToLast(ADS_LIMIT)
        } else {
            getAllAdsByFilterFirstPage(filter)
        }
        readDBData(query, readCallback)
    }

    fun getAllAdsByFilterFirstPage(tempFilter:String): Query{
        val orderBy = tempFilter.split("|")[0]
        val filter = tempFilter.split("|")[1]
        return db.orderByChild("/adFilter/$orderBy").startAt(filter).endAt(filter + "\uf8ff").limitToLast(ADS_LIMIT)
    }


    fun getAllAdsNextPage(time: String, filter: String, readCallback: ReadDataCallback?){
        if(filter.isEmpty()){
            val query = db.orderByChild(FILTER_TIME_PATH).endBefore(time).limitToLast(ADS_LIMIT)
            readDBData(query, readCallback)
        } else {
            getAllAdsByFilterNextPage(filter, time, readCallback)
        }
    }

    private fun getAllAdsByFilterNextPage(tempFilter:String, time:String, readCallback: ReadDataCallback?) {
        val orderBy = tempFilter.split("|")[0]
        val filter = tempFilter.split("|")[1]
        val query = db.orderByChild("/adFilter/$orderBy").endBefore(filter + "_$time").limitToLast(ADS_LIMIT)
        readNextPageFromDB(query, filter, orderBy, readCallback)
    }

    fun getAllAdsFromCatFirstPage(cat:String, filter: String, readCallback: ReadDataCallback?){
        val query = if(filter.isEmpty()){
            db.orderByChild(FILTER_CATTIME_PATH).startAt(cat).endAt(cat + "_\uf8ff").limitToLast(ADS_LIMIT)
        } else {
            getAllAdsFromCatByFilterFirstPage(cat, filter)
        }
        readDBData(query, readCallback)
    }

    fun getAllAdsFromCatByFilterFirstPage( cat:String, tempFilter:String): Query{
        val orderBy = "cat_" + tempFilter.split("|")[0]
        val filter = cat + "_" + tempFilter.split("|")[1]
        return db.orderByChild("/adFilter/$orderBy").startAt(filter).endAt(filter + "\uf8ff").limitToLast(ADS_LIMIT)
    }

    fun getAllAdsFromCatNextPage(cat:String, time:String, filter: String, readCallback: ReadDataCallback?){
        if (filter.isEmpty()){
            val query = db.orderByChild(FILTER_CATTIME_PATH).endBefore(cat + "_" + time).limitToLast(ADS_LIMIT)
            readDBData(query, readCallback)
        }else{
            getAllAdsFromCatByFilterNextPage(cat, time, filter, readCallback)
        }
    }

    private fun getAllAdsFromCatByFilterNextPage(cat: String, time:String, tempFilter:String, readCallback: ReadDataCallback?) {
        val orderBy = "cat_" + tempFilter.split("|")[0]
        val filter = cat + "_" + tempFilter.split("|")[1]
        val query = db.orderByChild("/adFilter/$orderBy").endBefore(filter + "_" + time).limitToLast(ADS_LIMIT)
        readNextPageFromDB(query, filter, orderBy, readCallback)
    }

    fun deleteAd(ad:Ad, listener:FinishWorkListener){
        if(ad.key == null || ad.uid == null) return
        db.child(ad.key).child(ad.uid).removeValue().addOnCompleteListener {
            if(it.isSuccessful) listener.onFinish(true)
        }
    }

    private fun readDBData(query: Query,readCallback: ReadDataCallback?){
        query.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
            val adArray = ArrayList<Ad>()
            for(item in snapshot.children){
                var ad: Ad? = null
                item.children.forEach {
                    if (ad == null) ad = it.child(AD_NODE).getValue(Ad::class.java)
                }
                val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)

                val favCounter = item.child(FAVS_NODE).childrenCount
                val isFav = auth.uid?.let { item.child(FAVS_NODE).child(it).getValue(String::class.java) }
                ad?.isFav = isFav != null
                ad?.favCounter = favCounter.toString()

                ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                ad?.emailCounter = infoItem?.emailsCounter ?: "0"
                ad?.callsCounter = infoItem?.callsCounter ?: "0"
                if(ad != null) adArray.add(ad!!)
                }
                readCallback?.readData(adArray)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun readNextPageFromDB(query: Query, filter: String, orderBy:String, readCallback: ReadDataCallback?){
        query.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>()
                for(item in snapshot.children){
                    var ad: Ad? = null
                    item.children.forEach {
                        if (ad == null) ad = it.child(AD_NODE).getValue(Ad::class.java)
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)
                    val filterNodeValue = item.child(FILTER_NODE).child(orderBy).value.toString()

                    val favCounter = item.child(FAVS_NODE).childrenCount
                    val isFav = auth.uid?.let { item.child(FAVS_NODE).child(it).getValue(String::class.java) }
                    ad?.isFav = isFav != null
                    ad?.favCounter = favCounter.toString()

                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    if(ad != null && filterNodeValue.startsWith(filter)) adArray.add(ad!!)
                }
                readCallback?.readData(adArray)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    interface ReadDataCallback {
        fun readData(list:ArrayList<Ad>)
    }
    interface FinishWorkListener{
        fun onFinish(isDone: Boolean)
    }

    companion object{
        const val AD_NODE = "ad"
        const val FILTER_NODE = "adFilter"
        const val MAIN_NODE = "main"
        const val INFO_NODE = "info"
        const val FAVS_NODE = "favs"
        const val ADS_LIMIT = 5
        const val FILTER_TIME_PATH = "/adFilter/time"
        const val FILTER_CATTIME_PATH = "/adFilter/cat_time"

    }
}

