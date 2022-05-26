package com.andrio_kt_dev.billboard.activ

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.andrio_kt_dev.billboard.R
import com.andrio_kt_dev.billboard.databinding.ActivityFilterBinding
import com.andrio_kt_dev.billboard.dialoghelper.DialogSpinnerHelper
import com.andrio_kt_dev.billboard.utils.CityHelper

class FilterActivity : AppCompatActivity() {
    lateinit var binding: ActivityFilterBinding
    private val dialog = DialogSpinnerHelper()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onClickSelectCountry()
        onClickSelectCity()
        actionBarSettings()
        onClickDoneFilter()
        onClickClearFilter()
        getFilter()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
    fun actionBarSettings(){
        val bar = supportActionBar
        bar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun getFilter() = with(binding){
        val filter = intent.getStringExtra(FILTER_KEY)
        if(filter != null && filter != "empty") {
            val filterArray = filter.split("_")
            if(filterArray[0]!= "empty") tvCountrySelection.text = filterArray[0]
            if(filterArray[1]!= "empty") tvCitySelection.text = filterArray[1]
            if(filterArray[2]!= "empty") edIndex.setText(filterArray[2])
            cbSend.isChecked = filterArray[3].toBoolean()
        }
    }

    private fun onClickSelectCountry() = with(binding) {
        tvCountrySelection.setOnClickListener {
            val listCountry = CityHelper.getAllCountries(this@FilterActivity)
            dialog.showSpinnerDialog(this@FilterActivity, listCountry, tvCountrySelection)
            if (tvCitySelection.text.toString() != getString(R.string.select_city)) {
                tvCitySelection.text = getString(R.string.select_city)
            }
        }
    }

    private fun onClickSelectCity() = with(binding){
        tvCitySelection.setOnClickListener {
            val selectedCountry = tvCountrySelection.text.toString()
            if(selectedCountry != getString(R.string.select_country)) {
                val listCity = CityHelper.getAllCities(selectedCountry, this@FilterActivity)
                dialog.showSpinnerDialog(this@FilterActivity, listCity,tvCitySelection)
            } else Toast.makeText(this@FilterActivity, getString(R.string.country_not_selected), Toast.LENGTH_LONG ).show()
        }
    }

    private fun onClickDoneFilter() = with(binding){
        btDoneFilter.setOnClickListener {
            val intent = Intent().apply{
                putExtra(FILTER_KEY, createFilter())
            }
            setResult(RESULT_OK, intent)
            finish()
        }

    }
    private fun onClickClearFilter() = with(binding){
        btClearFilter.setOnClickListener {
            tvCountrySelection.text = getString(R.string.select_country)
            tvCitySelection.text = getString(R.string.select_city)
            edIndex.setText("")
            cbSend.isChecked = false
            setResult(RESULT_CANCELED)
        }
    }

    private fun createFilter():String = with(binding){
        val sBuilder = StringBuilder()
        val arrayTempFilter = listOf(tvCountrySelection.text,
            tvCitySelection.text,
            edIndex.text,
            cbSend.isChecked.toString())
        for((i, s) in arrayTempFilter.withIndex()){
            if(s != getString(R.string.select_country) && s!= getString(R.string.select_city) && s.isNotEmpty()){
                sBuilder.append(s)
                if(i != arrayTempFilter.size-1)sBuilder.append("_")
            } else {
                sBuilder.append("empty")
                if(i != arrayTempFilter.size-1)sBuilder.append("_")
            }
        }
        return sBuilder.toString()
    }

    companion object{
        const val FILTER_KEY = "Filter_key"
    }
 }