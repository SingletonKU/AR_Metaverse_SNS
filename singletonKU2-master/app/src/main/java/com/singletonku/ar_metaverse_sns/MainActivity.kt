package com.singletonku.ar_metaverse_sns

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.singletonku.ar_metaverse_sns.navigation.*
import com.singletonku.ar_metaverse_sns.navigation.HashtagFragment
import com.singletonku.ar_metaverse_sns.navigation.model.HashTagViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest


class MainActivity : AppCompatActivity(){

    val myViewModel : HashTagViewModel by viewModels<HashTagViewModel>()

    //val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navi)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navi)


        bottomNavigation.setOnItemSelectedListener { it ->
            setToolbarDefault()

            when (it.itemId) {
                R.id.action_home -> {
                    val detailViewFragment = DetailViewFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.main_content, detailViewFragment).commit()
                    true
                }
                R.id.action_search -> {
                    val hashtagFragment = HashtagFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.main_content, hashtagFragment).commit()
                    true
                }
                R.id.action_photo -> {
                    if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        startActivity(Intent(this, AddPhotoActivity::class.java))
                    }
                    true
                }

                R.id.action_ar -> {
                    val ArFragment = ArFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.main_content, ArFragment).commit()
                    true
                }

                R.id.action_account -> {
                    val userFragment = UserFragment()
                    var bundle = Bundle()
                    var uid = FirebaseAuth.getInstance().currentUser?.uid
                    bundle.putString("destinationUid", uid)
                    userFragment.arguments = bundle
                    supportFragmentManager.beginTransaction().replace(R.id.main_content, userFragment).commit()
                    true
                }
                else -> false
            }
        }



        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        //Set default screen
        bottomNavigation.selectedItemId = R.id.action_home

        var hashtagEvent : String? = intent.getStringExtra("hashtagEvent")
        if (hashtagEvent != null){
            myViewModel.setLiveData(hashtagEvent)
            hashtagEvent = null
            bottomNavigation.selectedItemId = R.id.action_search
        }
    }

    fun setToolbarDefault(){
        toolbar_username.visibility = View.GONE
        toolbar_btn_back.visibility = View.GONE
        toolbar_title_image.visibility = View.VISIBLE
    }
}


