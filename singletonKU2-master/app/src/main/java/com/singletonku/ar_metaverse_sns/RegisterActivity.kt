package com.singletonku.ar_metaverse_sns

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import android.widget.EditText
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.singletonku.ar_metaverse_sns.R
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.singletonku.ar_metaverse_sns.UserAccount
import android.widget.Toast

class RegisterActivity : AppCompatActivity() {
    private var mFirebaseAuth //파이어베이스 인증
            : FirebaseAuth? = null
    private var mDatabaseRef // 실시간 데이터베이스
            : DatabaseReference? = null
    private var mEtEmail: EditText? = null
    private var mEtpwd // 회원가입 입력필드
            : EditText? = null
    private var mBtnRegister // 회원가입 버튼
            : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("ar_sns")
        mEtEmail = findViewById(R.id.et_email)
        mEtpwd = findViewById(R.id.et_pwd)
        mBtnRegister = findViewById(R.id.btn_register)
        mBtnRegister?.setOnClickListener(View.OnClickListener {
            //회원가입 처리 시작
            val strEmail = mEtEmail?.getText().toString()
            val strPwd = mEtpwd?.getText().toString()

            // Firebase auth 진행
            mFirebaseAuth!!.createUserWithEmailAndPassword(strEmail, strPwd)
                .addOnCompleteListener(this@RegisterActivity) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = mFirebaseAuth!!.currentUser
                        val account = UserAccount()
                        account.idToken = firebaseUser!!.uid
                        account.emailId = firebaseUser.email
                        account.password = strPwd

                        //setvalue = database에 insert
                        mDatabaseRef!!.child("UserAccount").child(firebaseUser.uid)
                            .setValue(account)
                        Toast.makeText(this@RegisterActivity, "회원가입에 성공하셨습니다", Toast.LENGTH_SHORT)
                            .show()

                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish() // 현재 엑티비티 파괴

                    } else {
                        Toast.makeText(this@RegisterActivity, "아이디가 이메일 형식이어야 합니다", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        })
    }
}