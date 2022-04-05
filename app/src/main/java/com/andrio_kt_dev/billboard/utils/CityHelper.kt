package com.andrio_kt_dev.billboard.utils

import android.content.Context
import com.andrio_kt_dev.billboard.R
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

object CityHelper {
    fun getAllCountries(context: Context) :ArrayList<String> {
        val tempCountries = ArrayList<String>()
        try {
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size:Int = inputStream.available()
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            val jsonFile = String(bytesArray)

            val jsonObject = JSONObject(jsonFile)
            val countryName = jsonObject.names()
            if(countryName != null){
                for (n in 0 until countryName.length()){
                    tempCountries.add(countryName.getString(n))
                }
            }
        } catch (e:IOException){

        }
        return tempCountries
    }

    fun getAllCities(country: String, context: Context) :ArrayList<String> {
        val tempCities = ArrayList<String>()
        try {
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size:Int = inputStream.available()
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            val jsonFile = String(bytesArray)

            val jsonObject = JSONObject(jsonFile)
            val cityName = jsonObject.getJSONArray(country)
                for (n in 0 until cityName.length()){
                    tempCities.add(cityName.getString(n))
                }
        } catch (e:IOException){

        }
        return tempCities
    }

    fun filterListData(list:ArrayList<String>,searchText:String?, context: Context):ArrayList<String>{
        val tempList = ArrayList<String>()
        tempList.clear()
        if(searchText == null){
            tempList.add(context.resources.getString(R.string.no_result))
            return tempList
        }
        for (selection:String in list){
            if(selection.lowercase().startsWith(searchText.lowercase())){
                tempList.add(selection)
            }
        }
        if (tempList.size == 0) tempList.add(context.resources.getString(R.string.no_result))
        return tempList
    }
}