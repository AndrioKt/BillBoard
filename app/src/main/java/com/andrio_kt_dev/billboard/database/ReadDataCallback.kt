package com.andrio_kt_dev.billboard.database

import com.andrio_kt_dev.billboard.data.Ad

interface ReadDataCallback {
    fun readData(list:List<Ad>)
}