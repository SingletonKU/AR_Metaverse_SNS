package com.singletonku.ar_metaverse_sns.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.singletonku.ar_metaverse_sns.MainActivity
import com.singletonku.ar_metaverse_sns.R
import com.singletonku.ar_metaverse_sns.databinding.ActivityUploadedViewBinding
import com.singletonku.ar_metaverse_sns.databinding.ItemDetailBinding
import com.singletonku.ar_metaverse_sns.navigation.model.ContentDTO
import java.util.regex.Matcher
import java.util.regex.Pattern

class UploadedViewActivity : AppCompatActivity() {
    lateinit var activityBinding: ActivityUploadedViewBinding
    var uid: String? = ""
    var firestore: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityBinding = ActivityUploadedViewBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)

        uid = intent.getStringExtra("userId")
        firestore = FirebaseFirestore.getInstance()

        activityBinding.upviewRecyclerview.adapter = UploadedViewRVAdapter()
        activityBinding.upviewRecyclerview.layoutManager = LinearLayoutManager(this)

        activityBinding.upviewBtnBack.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    inner class UploadedViewRVAdapter : RecyclerView.Adapter<UploadedViewRVAdapter.MyViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            var ref =
                firestore?.collection("images")?.whereEqualTo("uid", uid!!)
                    ?.orderBy("timestamp")

            Log.d("my UID", uid!!)


            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { value, error2 ->
                    ref?.addSnapshotListener { querySnapshot, error ->
                        var newContentDTOs: ArrayList<ContentDTO> = arrayListOf()
                        var newContentUidList: ArrayList<String> = arrayListOf()
                        newContentDTOs.clear()
                        newContentUidList.clear()

                        Log.d("snap size", "null 확인후 리턴 직전")

                        //sometimes, This code return null of qeurySnapshot when it signout
                        if (querySnapshot == null) return@addSnapshotListener

                        Log.d("snap size", "null 아님 확인, size : " + querySnapshot.size())
                        for (snapshot in querySnapshot!!.documents) {
                            var item = snapshot.toObject(ContentDTO::class.java)
                            newContentDTOs.add(item!!)
                            newContentUidList.add(snapshot.id)
                        }

                        contentDTOs = newContentDTOs
                        contentUidList = newContentUidList
                        notifyDataSetChanged()
                        Log.d("my CONTENTS", "size : " + contentDTOs.size)
                    }
                }
        }

        inner class MyViewHolder(val binding: ItemDetailBinding) :
            RecyclerView.ViewHolder(binding.root) {

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = ItemDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            //userId
            holder.binding.detailviewitemProfileTextview.text = contentDTOs!![position].userId

            //Image
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
                .into(holder.binding.detailviewitemImageviewContent)

            //explain of content
            holder.binding.detailviewitemExplainTextview.text = contentDTOs!![position].explain

            //hashtag of content
            var hashtagList: ArrayList<String> = contentDTOs!![position].hashtagList
            if (hashtagList != null) {
                var msg: String = ""
                for (j in hashtagList) {
                    msg = msg + j + " "
                    Log.d("msg", msg)
                }

                holder.binding.detailviewitemHashtagTextview.text = msg
                var hashtagView = holder.binding.detailviewitemHashtagTextview
                getContent(msg, hashtagView)
            }

            //likes
            holder.binding.detailviewitemFavoritecounterTextview.text =
                "Likes " + contentDTOs!![position].favoriteCount

            //profileImage
            /*Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
                .into(holder.binding.detailviewitemProfileImage)
            */
            firestore?.collection("profileImages")?.document(contentDTOs!![position].uid!!)
                ?.addSnapshotListener { documentSnapshot, error ->
                    if (documentSnapshot == null) return@addSnapshotListener

                    if (documentSnapshot.data != null) {
                        var url = documentSnapshot?.data!!["image"]

                        Glide.with(holder.itemView.context).load(url)
                            .apply(RequestOptions().circleCrop())
                            .into(holder.binding.detailviewitemProfileImage)
                    }

                }

            //This code is when the button is clicked
            holder.binding.detailviewitemFavoriteImageview.setOnClickListener {
                favoriteEvent(position)
            }

            //This code is when the page is loaded
            if (contentDTOs!![position].favorites.containsKey(uid)) {
                //This is like status
                holder.binding.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_baseline_favorite_24)
            } else {
                //This is unlike status
                holder.binding.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite_border)
            }

            //This code is when the profile image is clicked

            holder.binding.detailviewitemProfileImage.setOnClickListener {

                /*var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments = bundle
                supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.main_content, fragment)?.commit()

                 */
                setResult(Activity.RESULT_OK)
                finish()
            }

            holder.binding.detailviewitemCommentImageview.setOnClickListener { v ->
                var intent = Intent(v.context, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        fun favoriteEvent(position: Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->

                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if (contentDTO!!.favorites.containsKey(uid)) {
                    //when the button is clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)
                } else {
                    //when the button is not clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    contentDTO?.favorites[uid!!] = true
                }

                transaction.set(tsDoc, contentDTO)
            }
        }


        fun getContent(tag: String, hashtagView: TextView) {

            Log.d("hash", "getContent 함수내의 hashtagText == null확인해서 리턴하기 직전, hashtagText : " + tag)
            if (tag == null) return

            var hashtagSpans: ArrayList<ArrayList<Int>> = getSpans(tag, '#')

            Log.d("hash", "getContent 함수내의 tagsContent 선언")
            var tagsContent: SpannableString = SpannableString(tag)

            if (hashtagSpans.size != 0) {
                for (i in 0..hashtagSpans.size - 1) {
                    var span: ArrayList<Int> = hashtagSpans.get(i)
                    var hashTagStart = span[0]
                    var hashTagEnd = span[1]

                    var hashTag = Hashtag(applicationContext)
                    hashTag.setOnHashtagClickEventListener(object :
                        Hashtag.HashtagClickEventListener {

                        override fun onHashtagClickEvent(data: String, context: Context) {
                            var myIntent = Intent(context, MainActivity::class.java)
                            myIntent.putExtra("hashtagEvent", data)
                            startActivity(myIntent)
                        }
                    })

                    tagsContent?.setSpan(hashTag, hashTagStart, hashTagEnd, 0)
                }

                hashtagView.movementMethod = LinkMovementMethod.getInstance()
                hashtagView.text = tagsContent
            }
            Log.d("hash", "getContent 함수내의 정상 리턴 직전")

        }

        fun getSpans(body: String, prefix: Char): ArrayList<ArrayList<Int>> {
            var spans: ArrayList<ArrayList<Int>> = ArrayList<ArrayList<Int>>()

            var pattern: Pattern = Pattern.compile(prefix + "\\S+")
            var matcher: Matcher = pattern.matcher(body)

            //check all occurrences
            while (matcher.find()) {
                var currentSpan = arrayListOf<Int>(matcher.start(), matcher.end())
                spans.add(currentSpan)
            }

            return spans
        }

    }


}