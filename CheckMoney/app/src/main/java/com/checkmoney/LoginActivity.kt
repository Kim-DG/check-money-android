package com.checkmoney

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    //google client
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var btn_join: Button
    private lateinit var btn_login: Button
    private lateinit var btn_signInButton: SignInButton
    private lateinit var et_id: EditText
    private lateinit var et_pw: EditText
    private lateinit var text_discorrect: TextView
    private lateinit var userId: String
    private lateinit var userPw: String

    private val RC_SIGN_IN = 99
    private val TAG = "LoginActivity"
    private val TAG2 = "LoginActivity_API"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //변수 초기화
        setVariable()
        //id, pw입력
        checkInput()

        autoLogin()

        googleBuildIn()

        btn_join.setOnClickListener{
            val dialog = JoinPopupActivity(this@LoginActivity)
            dialog.start()
        }

        btn_login.setOnClickListener {
            if(userId != "" && userPw != ""){
                val userInfo = UserInfo(userId,userPw)
                postLogin(userInfo)
            }
            if(userId == "" && userPw != ""){
                text_discorrect.text = "아이디를 입력해 주세요."
            }
            if(userId != "" && userPw == ""){
                text_discorrect.text = "비밀번호를 입력해 주세요."
            }
            if(userId == "" && userPw == ""){
                text_discorrect.text = "아이디와 비밀번호를 입력해 주세요."
            }
        }

        btn_signInButton.setOnClickListener {
            val gsa = GoogleSignIn.getLastSignedInAccount(this@LoginActivity)
            if (gsa?.id != null) {
                val Intent = Intent(this, MainActivity::class.java)
                startActivity(Intent)
            }
            else signIn()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun setVariable() {
        btn_join = findViewById(R.id.btn_join)
        btn_login = findViewById(R.id.btn_login)
        btn_signInButton = findViewById(R.id.sign_in_button)
        et_id = findViewById(R.id.et_id)
        et_pw = findViewById(R.id.et_pw)
        text_discorrect = findViewById(R.id.text_discorrect)
    }

    //자동으로 id,pw 입력
    private fun autoLogin() {
        et_id.setText(AppPref.prefs.myId)
        et_pw.setText(AppPref.prefs.myPw)

        if(AppPref.prefs.myId.isNullOrBlank()
            || AppPref.prefs.myPw.isNullOrBlank()) {
        }
        else { // SharedPreferences 안에 값이 저장되어 있을 때 -> MainActivity로 이동
            val userInfo = UserInfo(AppPref.prefs.myId!!, AppPref.prefs.myPw!!)
            postLogin(userInfo)
        }
    }

    //입력한 id, pw 저장
    private fun checkInput(){
        et_id.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userId = et_id.text.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        et_pw.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userPw = et_pw.text.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateUI() {
        val Intent = Intent(this, MainActivity::class.java)
        startActivity(Intent)
    }

    //-----------------------------------------------------------------------
    //                             Google Login
    //-----------------------------------------------------------------------

    private fun googleBuildIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("500159069581-m2dqev5jhbpumksnoodl7bmi90v5kjtl.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>?) {
        try{
            val account = task?.getResult(ApiException::class.java)
            if(account != null){
                val name = account.displayName
                val email = account.email
                val idToken = account.idToken
                Log.d(TAG,"Name = $name")
                Log.d(TAG,"email = $email")
                Log.d(TAG,"idToken = $idToken")
                updateUI()

                val idTokenClass = IdToken(id_token = idToken)

                postgoogle(idTokenClass)
            }

        }catch (e: ApiException){
            Log.d(TAG, "Google sign in failed", e)
        }
    }

    //구글 로그인
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() { // 로그아웃
        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this) {
        //updateUI(null)
            Log.d(TAG, "Logout success")
            /*
            googleSignInClient.revokeAccess().addOnCompleteListener(this){
                Log.d(TAG, "revokeAccess success")
            }
            */
        }
    }

    //-----------------------------------------------------------------------
    //                            Rest Api function
    //-----------------------------------------------------------------------

    private fun postgoogle(idToken: IdToken) {
        RetrofitBuild.api.postGoogle(idToken).enqueue(object : Callback<ResultAndToken>{
            override fun onResponse(call: Call<ResultAndToken>, response: Response<ResultAndToken>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val a: ResultAndToken = response.body()!!
                    Log.d(TAG2,a.toString())
                } else { // code == 400
                    // 실패 처리
                    Log.d(TAG2, "연결실패")
                }
            }
            override fun onFailure(call: Call<ResultAndToken>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }

    private fun postLogin(userInfo: UserInfo) {
        RetrofitBuild.api.postLogin(userInfo).enqueue(object : Callback<ResultAndToken>{
            override fun onResponse(call: Call<ResultAndToken>, response: Response<ResultAndToken>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val a = response.body()
                    Log.d(TAG2,a.toString())
                    if(a?.result == "true") {
                        AppPref.prefs.myId = userId
                        AppPref.prefs.myPw = userPw
                        val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(mainIntent)
                    }
                    else
                        text_discorrect.text = "아이디, 비밀번호가 일치하지 않습니다."
                } else { // code == 400
                    // 실패 처리
                    Log.d(TAG2, "연결실패")
                    text_discorrect.text = "아이디, 비밀번호가 일치하지 않습니다."
                }
            }
            override fun onFailure(call: Call<ResultAndToken>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
                text_discorrect.text = "네트워크 문제가 발생했습니다."
            }
        })
    }
}
