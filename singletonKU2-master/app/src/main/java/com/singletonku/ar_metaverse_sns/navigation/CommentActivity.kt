package com.singletonku.ar_metaverse_sns.navigation

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.singletonku.ar_metaverse_sns.R
import com.singletonku.ar_metaverse_sns.databinding.ItemCommentBinding
import com.singletonku.ar_metaverse_sns.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {
    var contentUid: String? = null
    lateinit var firestore : FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        setContentView(R.layout.activity_comment)
        contentUid = intent.getStringExtra("contentUid")

        comment_recyclerview.adapter = CommentRecyclerviewAdapter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this)

        comment_btn_send?.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_edit_message.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                .collection("comments").document().set(comment)
            comment_edit_message.setText("")
        }

        cmtview_btn_back.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }

    }

    inner class CommentRecyclerviewAdapter :
        RecyclerView.Adapter<CommentRecyclerviewAdapter.MyViewHolder>() {

        var comments: ArrayList<ContentDTO.Comment> = arrayListOf()
        var commentsUids: ArrayList<String> = arrayListOf()

        init {
            recyclerDataInit()

        }

        inner class MyViewHolder(val binding: ItemCommentBinding) :
            RecyclerView.ViewHolder(binding.root) {
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            var itemCommentBinding =
                ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            var viewHolder = MyViewHolder(itemCommentBinding)
            return viewHolder
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.binding.commentviewitemTextviewComment.text = comments[position].comment
            holder.binding.commentviewitemTextviewProfile.text = comments[position].userId

            /*
            firestore?.collection("profileImages")?.document(comments[position].uid!!)
                ?.addSnapshotListener { documentSnapshot, error ->
                    if (documentSnapshot == null) return@addSnapshotListener

                    if (documentSnapshot.data != null) {
                        var url = documentSnapshot?.data!!["image"]
                        Glide.with(holder.itemView.context).load(url)
                            .apply(RequestOptions().circleCrop())
                            .into(holder.binding.commentviewitemImageviewProfile)
                    }

                }

             */

            firestore
                .collection("profileImages")
                .document(comments[position].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var url = task.result!!["image"]


                        Glide.with(holder.itemView.context).load(url)
                            .apply(RequestOptions().circleCrop())
                            .into(holder.binding.commentviewitemImageviewProfile)
                    }
                }


            holder.binding.commentLayout.setOnLongClickListener {
                val dlgBuilder = AlertDialog.Builder(this@CommentActivity)
                    .setTitle("댓글 삭제")
                    .setMessage("해당 댓글을 삭제하시겠습니까?")
                    .setIcon(R.drawable.ic_baseline_delete_forever_24)
                    .setPositiveButton("삭제") { _, _ ->


                        firestore
                            .collection("images")
                            .document(contentUid!!)
                            .collection("comments")
                            .document(commentsUids[position])
                            .delete()
                            .addOnSuccessListener {
                                recyclerDataInit()
                                Log.d("댓글 삭제", "댓글 삭제 성공!")
                            }
                            .addOnFailureListener { e -> Log.w("댓글 삭제", "댓글삭제 실패 ㅠㅠ", e) }
                    }
                    .setNegativeButton("취소") { _, _ ->

                    }

                dlgBuilder.show()
                true
            }
        }

        override fun getItemCount(): Int {
            return comments.size
        }

        fun recyclerDataInit(){
            firestore
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, error ->
                    comments.clear()
                    commentsUids.clear()

                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot.documents) {
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                        commentsUids.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

    }

}