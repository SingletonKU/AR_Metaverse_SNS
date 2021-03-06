package com.singletonku.ar_metaverse_sns.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.singletonku.ar_metaverse_sns.R
import com.singletonku.ar_metaverse_sns.databinding.FragmentHashtagBinding
import com.singletonku.ar_metaverse_sns.databinding.ItemDetailBinding
import com.singletonku.ar_metaverse_sns.navigation.model.ContentDTO
import com.singletonku.ar_metaverse_sns.navigation.model.HashTagViewModel
import java.util.regex.Matcher
import java.util.regex.Pattern

class HashtagFragment : Fragment() {

    lateinit var fragBinding: FragmentHashtagBinding
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var stringHashtag: String? = ""

    val myViewModel : HashTagViewModel by activityViewModels<HashTagViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        stringHashtag = "#" + myViewModel.selectedHashtag.value
        if(stringHashtag!= null) {
            Log.d("stringHashtag", "name : " + stringHashtag)
        }
        fragBinding = FragmentHashtagBinding.inflate(inflater, container, false)

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        fragBinding.hashtagfragRecyclerview.adapter = HashtagRecyclerviewAdapter()
        fragBinding.hashtagfragRecyclerview.layoutManager = LinearLayoutManager(activity)

        return fragBinding.root
    }


    inner class HashtagRecyclerviewAdapter :
        RecyclerView.Adapter<HashtagRecyclerviewAdapter.MyViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            recyclerDataInit()

            fragBinding.hashtagfragIvResearch.setOnClickListener {
                var searchString : String = fragBinding.hashtagfragEtHashtag.text.toString()
                myViewModel.setLiveData(searchString)

                recyclerDataInit()
            }
        }

        inner class MyViewHolder(val binding: ItemDetailBinding) :
            RecyclerView.ViewHolder(binding.root) {

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            var viewBinding =
                ItemDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return MyViewHolder(viewBinding)
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
                getContent(hashtagView.text.toString(), hashtagView)
            }


            //likes
            holder.binding.detailviewitemFavoritecounterTextview.text =
                "Likes " + contentDTOs!![position].favoriteCount

            //profileImage
            /*
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
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
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.main_content, fragment)?.commit()
            }

            holder.binding.detailviewitemCommentImageview.setOnClickListener { v ->
                var intent = Intent(v.context, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                startActivity(intent)
            }

            var checkId = FirebaseAuth.getInstance().currentUser?.uid
            if (checkId != contentDTOs[position].uid) {
                holder.binding.detailviewitemProfileDelete.visibility = View.GONE
            }
            else{
                holder.binding.detailviewitemProfileDelete.visibility = View.VISIBLE
            }

            holder.binding.detailviewitemProfileDelete.setOnClickListener {

                //?????? ???????????? ????????? ????????? ??????????????? ??????
                var checkId = FirebaseAuth.getInstance().currentUser?.uid
                if (checkId == contentDTOs[position].uid) {
                    val dlgBuilder = AlertDialog.Builder(this@HashtagFragment.requireContext())
                        .setTitle("????????? ??????")
                        .setMessage("?????? ???????????? ?????????????????????????")
                        .setIcon(R.drawable.ic_baseline_delete_forever_24)
                        .setPositiveButton("??????") { _, _ ->
                            firestore
                                ?.collection("images")
                                ?.document(contentUidList[position])
                                ?.delete()
                                ?.addOnSuccessListener {
                                    recyclerDataInit()
                                    Log.d("????????? ??????", "????????? ?????? ??????!")
                                }
                                ?.addOnFailureListener { e -> Log.w("????????? ??????", "????????? ?????? ?????? ??????", e) }
                        }
                        .setNegativeButton("??????") { _, _ ->

                        }

                    dlgBuilder.show()
                }
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

            Log.d("hash", "getContent ???????????? hashtagText == null???????????? ???????????? ??????, hashtagText : " + tag)
            if (tag == null) return

            var hashtagSpans: ArrayList<ArrayList<Int>> = getSpans(tag, '#')

            Log.d("hash", "getContent ???????????? tagsContent ??????")
            var tagsContent: SpannableString = SpannableString(tag)

            if (hashtagSpans.size != 0) {
                for (i in 0..hashtagSpans.size - 1) {
                    var span: ArrayList<Int> = hashtagSpans.get(i)
                    var hashTagStart = span[0]
                    var hashTagEnd = span[1]

                    var hashTag = Hashtag(this@HashtagFragment.requireContext())
                    hashTag.setOnHashtagClickEventListener(object :
                        Hashtag.HashtagClickEventListener {

                        override fun onHashtagClickEvent(data: String, context: Context) {
                            myViewModel.setLiveData(data)
                            recyclerDataInit()
                        }
                    })

                    tagsContent?.setSpan(hashTag, hashTagStart, hashTagEnd, 0)
                }

                hashtagView.movementMethod = LinkMovementMethod.getInstance()
                hashtagView.text = tagsContent
            }
            Log.d("hash", "getContent ???????????? ?????? ?????? ??????")

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

        fun recyclerDataInit(){
            stringHashtag = "#" + myViewModel.selectedHashtag.value
            if(stringHashtag!= null) {
                Log.d("stringHashtag", "name : " + stringHashtag)
            }

            var ref =
                firestore?.collection("images")?.whereArrayContainsAny("hashtagList", listOf(stringHashtag))
                    ?.orderBy("timestamp")

            var flag = false

            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { value, error2 ->
                    ref?.addSnapshotListener { querySnapshot, error ->
                        Log.d("snap size", "null ????????? ?????? ??????")

                        //sometimes, This code return null of qeurySnapshot when it signout
                        if (querySnapshot == null) {
                            if(flag != true){
                                contentDTOs.clear()
                                contentUidList.clear()
                                notifyDataSetChanged()
                            }
                            return@addSnapshotListener
                        }

                        flag = true

                        Log.d("returnSnapshot", "????????????~"+ flag)
                        var newContentDTOs: ArrayList<ContentDTO> = arrayListOf()
                        var newContentUidList: ArrayList<String> = arrayListOf()
                        newContentDTOs.clear()
                        newContentUidList.clear()



                        //Log.d("snap size", "null ?????? ??????, size : " + querySnapshot.size())
                        for (snapshot in querySnapshot!!.documents) {
                            var item = snapshot.toObject(ContentDTO::class.java)
                            newContentDTOs.add(item!!)
                            newContentUidList.add(snapshot.id)
                        }

                        contentDTOs = newContentDTOs
                        contentUidList = newContentUidList
                        notifyDataSetChanged()
                    }
                }


        }
    }

}