package com.singletonku.ar_metaverse_sns.navigation

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.singletonku.ar_metaverse_sns.LoginActivity
import com.singletonku.ar_metaverse_sns.MainActivity
import com.singletonku.ar_metaverse_sns.R
import com.singletonku.ar_metaverse_sns.databinding.FragmentUserBinding
import com.singletonku.ar_metaverse_sns.navigation.model.ContentDTO
import com.singletonku.ar_metaverse_sns.navigation.model.FollowDTO
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment() {

    var fragmentView: FragmentUserBinding? = null
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null
    var currentUid: String? = null
    var getContent: ActivityResultLauncher<Intent>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //사진을 선택했을 경우 처리하는 코드
        getContent = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            var imageUri: Uri? = result.data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid

            //이미지를 저장할 폴더 생성. 파일명으로 uid를 넣음.
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)

            //이미지 다운로드 주소를 받아옴
            if(imageUri != null) {
                storageRef.putFile(imageUri!!)
                    .continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                        return@continueWithTask storageRef.downloadUrl
                    }.addOnSuccessListener { uri ->
                    //return한 결과값이 이 리스너로 넘어온다.
                    var map = HashMap<String, Any>()
                    map["image"] = uri.toString()
                    FirebaseFirestore.getInstance().collection("profileImages").document(uid!!)
                        .set(map)
                }
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = FragmentUserBinding.inflate(LayoutInflater.from(activity), container, false)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUid = auth?.currentUser?.uid

        if (uid == currentUid) {
            //MyPage
            fragmentView?.accountBtnFollowSignout?.text = "LOG OUT"
            fragmentView?.accountBtnFollowSignout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }
        } else {
            //OtherUserPage
            fragmentView?.accountBtnFollowSignout?.text = "FOLLOW"
            var mainActivity = (activity as MainActivity)
            mainActivity?.toolbar_username?.text = arguments?.getString("userId")

            mainActivity?.toolbar_btn_back?.setOnClickListener {
                mainActivity.bottom_navi.selectedItemId = R.id.action_home
            }

            mainActivity?.toolbar_title_image?.visibility = View.GONE
            mainActivity?.toolbar_username?.visibility = View.VISIBLE
            mainActivity?.toolbar_btn_back?.visibility = View.VISIBLE

            fragmentView?.accountBtnFollowSignout?.setOnClickListener {
                requestFollow()
            }
        }

        fragmentView?.accountRecyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.accountRecyclerview?.layoutManager = GridLayoutManager(activity, 3)

        fragmentView?.accountIvProfile?.setOnClickListener {
            //var photoUri: Uri? = null


          /*  if(uid == currentUid) {
                //open the album
                var photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = MediaStore.Images.Media.CONTENT_TYPE
                photoPickerIntent.type = "image" //여기원래 뭐 더 있었음
                getContent?.launch(photoPickerIntent)
            }*/




        }

        getProfileImage()
        getFollowerAndFollowing()

        return fragmentView!!.root
    }



    inner class UserFragmentRecyclerViewAdapter :
        RecyclerView.Adapter<UserFragmentRecyclerViewAdapter.MyViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()

        init {
            firestore?.collection("images")?.whereEqualTo("uid", uid)
                ?.addSnapshotListener { querySanpshot, error ->
                    //sometimes, This code return null of qeurySnapshot when it signout
                    if (querySanpshot == null) return@addSnapshotListener

                    //Get Data
                    for (snapshot in querySanpshot) {
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    }
                    fragmentView?.accountTvPostCount?.text = contentDTOs.size.toString()
                    notifyDataSetChanged()
                }
        }

        inner class MyViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            var width = resources.displayMetrics.widthPixels / 3

            var imageview = ImageView(parent.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return MyViewHolder(imageview)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            var imageview = holder.imageview

            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl)
                .apply(RequestOptions().centerCrop()).into(imageview)

            imageview.setOnClickListener {
                var myIntent = Intent(this@UserFragment.requireContext(), UploadedViewActivity::class.java)
                myIntent.putExtra("userId", uid)
                startActivity(myIntent)
            }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }

    fun getProfileImage(){
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, error ->
            if (documentSnapshot == null) return@addSnapshotListener

            if(documentSnapshot.data != null){
                var url = documentSnapshot?.data!!["image"]
                Glide.with(this.requireActivity()).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.accountIvProfile!!)
            }
        }
    }

    fun requestFollow(){
        //Save data to my account.
        var tsDocFollowing = firestore?.collection("users")?.document(currentUid!!)
        firestore?.runTransaction { transaction->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                //followDTO!!.followers[uid!!] = true
                followDTO!!.followings[uid!!] = true

                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction
            }

            if (followDTO.followings.containsKey(uid)){
                //상대방 키가 있을 경우. 내가 팔로우 한 상태
                //It remove following third person when a third person follow me.
                followDTO?.followingCount = followDTO?.followingCount - 1
                //followDTO?.followers?.remove(uid)
                followDTO?.followings?.remove(uid)
            }
            else{
                //It add following third person when a third person do not follow me.
                followDTO?.followingCount = followDTO?.followingCount + 1
                //followDTO?.followers[uid!!] = true
                followDTO?.followings[uid!!] = true
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction

        }

        //Save data to third person.
        // 내가 팔로잉한 상대방 계정에 접근하는 코드
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUid!!] = true

                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }

            if(followDTO!!.followers.containsKey(currentUid)){
                //It cancel my follower when I follow a third person
                followDTO!!.followerCount = followDTO!!.followerCount -1
                followDTO!!.followers.remove(currentUid!!)
            }
            else{
                //It add my follower when I don't follow a third person
                followDTO!!.followerCount = followDTO!!.followerCount +1
                followDTO!!.followers[currentUid!!] = true
            }
            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction
        }
    }
    
    fun getFollowerAndFollowing(){
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, error ->
            if(documentSnapshot == null){
                return@addSnapshotListener
            }

            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO?.followerCount != null){
                fragmentView?.accountTvFollowerCount?.text = followDTO?.followerCount.toString()
                if (followDTO?.followers?.containsKey(currentUid!!)){
                    fragmentView?.accountBtnFollowSignout?.text = "FOLLOW CANCEL"
                    fragmentView?.accountBtnFollowSignout?.background?.setTint(Color.LTGRAY)
                }
                else{
                    if(uid != currentUid){
                        fragmentView?.accountBtnFollowSignout?.text = "FOLLOW"
                        fragmentView?.accountBtnFollowSignout?.background?.colorFilter = null
                    }
                }
            }
            if(followDTO?.followingCount != null){
                fragmentView?.accountTvFollowingCount?.text = followDTO?.followingCount.toString()
            }

        }
    }

}