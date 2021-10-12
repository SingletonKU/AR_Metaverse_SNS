package com.singletonku.ar_metaverse_sns

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import android.widget.EditText
import android.os.Bundle
import com.singletonku.ar_metaverse_sns.R
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import android.content.Intent
import android.widget.Button
import com.singletonku.ar_metaverse_sns.MainActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.singletonku.ar_metaverse_sns.RegisterActivity

class LoginActivity : AppCompatActivity() {
    private var mFirebaseAuth //파이어베이스 인증
            : FirebaseAuth? = null
    private var mDatabaseRef // 실시간 데이터베이스
            : DatabaseReference? = null
    private var mEtEmail: EditText? = null
    private var mEtpwd // 로그인 입력필드
            : EditText? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("ar_sns")
        mEtEmail = findViewById(R.id.et_email)
        mEtpwd = findViewById(R.id.et_pwd)
        val btn_login = findViewById<Button>(R.id.btn_login)
        btn_login.setOnClickListener {
            //로그인 요청
            val strEmil = mEtEmail?.getText().toString()
            val strPwd = mEtpwd?.getText().toString()
            mFirebaseAuth!!.signInWithEmailAndPassword(strEmil, strPwd)
                .addOnCompleteListener(this@LoginActivity) { task ->
                    if (task.isSuccessful) {
                        //로그인 성공
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // 현재 엑티비티 파괴
                    } else {
                        Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        val btn_register = findViewById<Button>(R.id.btn_register)
        btn_register.setOnClickListener { //회원가입 화면으로 이동
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        //moveMainPage(mFirebaseAuth?.currentUser)
    }

    fun moveMainPage(user : FirebaseUser?){
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}