package com.singletonku.ar_metaverse_sns.navigation

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.singletonku.ar_metaverse_sns.R
import com.singletonku.ar_metaverse_sns.databinding.FragmentDetailBinding
import com.singletonku.ar_metaverse_sns.databinding.ItemDetailBinding
import com.singletonku.ar_metaverse_sns.navigation.model.ContentDTO
import com.singletonku.ar_metaverse_sns.navigation.model.HashTagViewModel
import java.util.regex.Matcher
import java.util.regex.Pattern

class DetailViewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    var uid: String? = null

    val myViewModel: HashTagViewModel by activityViewModels<HashTagViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = FragmentDetailBinding.inflate(LayoutInflater.from(activity), container, false)

        //LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailviewfragmentRecyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragmentRecyclerview.layoutManager = LinearLayoutManager(activity)

        return view.root
    }


    inner class DetailViewRecyclerViewAdapter :
        RecyclerView.Adapter<DetailViewRecyclerViewAdapter.MyViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
           recyclerDataInit()
        }

        inner class MyViewHolder(val binding: ItemDetailBinding) :
            RecyclerView.ViewHolder(binding.root) {

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            var view = ItemDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

            holder.binding.detailviewitemProfileDelete.setOnClickListener {

                //해당 게시글이 자신이 작성한 게시물인지 체크
                var checkId = FirebaseAuth.getInstance().currentUser?.uid
                if (checkId == contentDTOs[position].uid) {
                    val dlgBuilder = AlertDialog.Builder(this@DetailViewFragment.requireContext())
                        .setTitle("게시글 삭제")
                        .setMessage("해당 게시글을 삭제하시겠습니까?")
                        .setIcon(R.drawable.ic_baseline_delete_forever_24)
                        .setPositiveButton("삭제") { _, _ ->
                            firestore
                                ?.collection("images")
                                ?.document(contentUidList[position])
                                ?.delete()
                                ?.addOnSuccessListener {
                                    recyclerDataInit()
                                    Log.d("게시글 삭제", "게시글 삭제 성공!")
                                }
                                ?.addOnFailureListener { e -> Log.w("게시글 삭제", "게시글 삭제 실패 ㅠㅠ", e) }
                        }
                        .setNegativeButton("취소") { _, _ ->

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

                    var hashTag = Hashtag(this@DetailViewFragment.requireContext())
                    hashTag.setOnHashtagClickEventListener(object :
                        Hashtag.HashtagClickEventListener {

                        override fun onHashtagClickEvent(data: String, context: Context) {

                            myViewModel.setLiveData(data)

                            val bottomNavigation =
                                activity?.findViewById<BottomNavigationView>(R.id.bottom_navi)
                            bottomNavigation?.selectedItemId = R.id.action_search

                            var fragment = HashtagFragment()
                            activity?.supportFragmentManager?.beginTransaction()
                                ?.replace(R.id.main_content, fragment)?.commit()

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


        fun recyclerDataInit(){
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, exception ->
                    contentDTOs.clear()
                    contentUidList.clear()

                    //sometimes, This code return null of qeurySnapshot when it signout
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }

                    notifyDataSetChanged()
                }

        }

    }
}