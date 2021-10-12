package com.singletonku.ar_metaverse_sns.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.singletonku.ar_metaverse_sns.MainActivity
import com.singletonku.ar_metaverse_sns.R
import com.singletonku.ar_metaverse_sns.databinding.ActivityAddPhotoBinding
import com.singletonku.ar_metaverse_sns.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.activity_comment.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class AddPhotoActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddPhotoBinding

    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null

    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initiate
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //open the album
        val getContent = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if(result.data == null){
                /*
                setResult(Activity.RESULT_OK)
                finish()

                 */
                setResult(Activity.RESULT_OK)

                var myIntent = Intent(this, MainActivity::class.java)
                startActivity(myIntent)
            }
            else{
                photoUri = result.data?.data
                binding.addphotoImage.setImageURI(result.data?.data)
            }

        }

        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = MediaStore.Images.Media.CONTENT_TYPE
        photoPickerIntent.type = "image/*"
        getContent.launch(photoPickerIntent)


        //add image upload event
        binding.addphotoBtnUpload.setOnClickListener {
            contentUpload()
        }

        binding.addviewBtnBack.setOnClickListener {
            setResult(Activity.RESULT_OK)

            var myIntent = Intent(this, MainActivity::class.java)
            startActivity(myIntent)

            /*setResult(Activity.RESULT_OK)
            finish()

             */
        }
    }

    fun contentUpload() {
        //make filename
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        //Promise method
        storageRef?.putFile(photoUri!!)?.continueWithTask { task : Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            //Insert downloadUrl of image
            contentDTO.imageUrl = uri.toString()

            //Insert uid of user
            contentDTO.uid = auth?.currentUser?.uid

            //Insert userId
            contentDTO.userId = auth?.currentUser?.email

            //Insert hashtag of content

            var body : String = binding.addphotoEditHashtag.text.toString()
            var hashArray : ArrayList<String>? = null

            if (body != null){
                hashArray = getSpans(body, '#')
            }

            if(hashArray != null){
                contentDTO.hashtagList.addAll(hashArray)
            }


            //Insert explain of content
            contentDTO.explain = binding.addphotoEditExplain.text.toString()

            //Insert timestamp
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)

            var myIntent = Intent(this, MainActivity::class.java)
            startActivity(myIntent)

        }

        //Callback method
        /*storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri->
                var contentDTO = ContentDTO()

                //Insert downloadUrl of image
                contentDTO.imageUrl = uri.toString()

                //Insert uid of user
                contentDTO.uid = auth?.currentUser?.uid

                //Insert userId
                contentDTO.userId = auth?.currentUser?.email

                //Insert explain of content
                contentDTO.explain = binding.addphotoEditExplain.text.toString()

                //Insert timestamp
                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK)

                finish()
            }
        }*/
    }



    fun getSpans(body: String, prefix: Char): ArrayList<String> {
        var spans: ArrayList<String> = arrayListOf<String>()

        var pattern: Pattern = Pattern.compile(prefix + "\\S+")
        var matcher: Matcher = pattern.matcher(body)

        //check all occurrences
        while (matcher.find()) {
            spans.add(matcher.group())
        }

        return spans
    }

}