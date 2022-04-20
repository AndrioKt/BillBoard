package com.andrio_kt_dev.billboard.database

import android.util.Log
import android.widget.Toast
import com.andrio_kt_dev.billboard.data.Ad
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DBManager(val readCallback: ReadDataCallback?) {
    val db = Firebase.database.getReference("main")
    val auth = Firebase.auth

    fun publishAd(ad: Ad){
       if(auth.uid != null) db.child(ad.key ?: "empty").child(auth.uid!!).child("ad").setValue(ad)
    }

    fun readDBData(){
        db.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
            val adArray = ArrayList<Ad>()
            for(item in snapshot.children){
                val ad = item.children.iterator().next().child("ad").getValue(Ad::class.java)
                if(ad != null) adArray.add(ad)
                }
                readCallback?.readData(adArray)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}