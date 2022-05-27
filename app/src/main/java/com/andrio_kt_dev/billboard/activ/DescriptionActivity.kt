package com.andrio_kt_dev.billboard.activ

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import com.andrio_kt_dev.billboard.R
import com.andrio_kt_dev.billboard.adapters.ImageAdapter
import com.andrio_kt_dev.billboard.databinding.ActivityDescriptionBinding
import com.andrio_kt_dev.billboard.model.Ad
import com.andrio_kt_dev.billboard.utils.ImageManager

class DescriptionActivity : AppCompatActivity() {
    lateinit var binding : ActivityDescriptionBinding
    lateinit var adapter: ImageAdapter
    private var ad:Ad? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        init()
        binding.fbPhone.setOnClickListener{ call() }
        binding.fbMail.setOnClickListener{ sendEmail()}
    }

    private fun init(){
        adapter = ImageAdapter()
        binding.apply {
            viewPager.adapter = adapter
        }
        getIntentFromMain()
        imageCounter()
    }

    private fun getIntentFromMain(){
        ad = intent.getSerializableExtra("AD") as Ad
        if (ad!= null) updateUI(ad!!)
    }

    private fun updateUI(ad:Ad){
        ImageManager.fillImageArray(ad, adapter)
        fillTextAd(ad)
    }

    private fun isWithSend(withSend: Boolean):String{
        return if(withSend) getString(R.string.Send_yes) else getString(R.string.Send_no)
    }
    private fun fillTextAd(ad: Ad) = with(binding){
        tvTitle.text = ad.title
        tvDesc.text = ad.description
        tvPrice.text = ad.price
        tvPhoneNumber.text = ad.phone
        tvMail.text = ad.email
        tvCountry.text = ad.country
        tvCity.text = ad.city
        tvIndex.text = ad.index
        tvWithSend.text = isWithSend(ad.send.toBoolean())
    }

    private fun call(){
        val callUri = "tel:${ad?.phone}"
        val iCall = Intent(Intent.ACTION_DIAL)
        iCall.data = callUri.toUri()
        startActivity(iCall)
    }

    private fun sendEmail(){
        val iSendEmail = Intent(Intent.ACTION_SEND)
        iSendEmail.type = "message/rfc822"
        iSendEmail.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ad?.email))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.send_email_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.send_email_text)+ " \"" + ad!!.title + "\"")
        }
        try{
            startActivity(Intent.createChooser(iSendEmail, "Open with "))
        } catch(e: ActivityNotFoundException){
            Toast.makeText(this, "You don't have this activity",Toast.LENGTH_LONG).show()
        }
    }

    private fun imageCounter(){
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imageCounter = "${position + 1}/${binding.viewPager.adapter?.itemCount}"
                binding.tvImageCounter.text =  imageCounter
            }
        })
    }
}