package com.singletonku.ar_metaverse_sns

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        startLoading()
    }

    fun startLoading(){
        var handler : Handler = Handler()
        handler.postDelayed(object : Runnable{
            override fun run() {
                var myintent = Intent(this@LoadingActivity, LoginActivity::class.java)
                startActivity(myintent)
                finish()
            }
        }, 2000)
    }


}