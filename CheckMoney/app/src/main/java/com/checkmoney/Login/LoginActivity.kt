package com.checkmoney

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.checkmoney.Login.JoinPopupActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private val gson = Gson()
    private val type = object : TypeToken<ErrorResult>() {}.type
    //google client
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var btn_join: Button
    private lateinit var btn_login: Button
    private lateinit var btn_findPassword: TextView
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

        // 변수 초기화
        setVariable()
        // id, pw입력
        checkInput()
        // 자동로그인
        autoLogin()
        // 구글로그인 세팅
        googleBuildIn()

        // 회원가입
        btn_join.setOnClickListener{
            val dialog = JoinPopupActivity(this@LoginActivity)
            dialog.start()
        }

        val gsa = GoogleSignIn.getLastSignedInAccount(this@LoginActivity)
        if (gsa?.id != null) {
            val idTokenClass = IdToken(id_token = gsa.idToken)
            Log.d("TAG",gsa.email!!)
            postGoogle(idTokenClass, gsa.email!!)
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
            GoogleSignIn()
        }

        btn_findPassword.setOnClickListener {
            //구현예정
        }
    }

    // 변수 초기화
    private fun setVariable() {
        btn_join = findViewById(R.id.btn_join)
        btn_login = findViewById(R.id.btn_login)
        btn_signInButton = findViewById(R.id.sign_in_button)
        btn_findPassword = findViewById(R.id.btn_findpasword)
        et_id = findViewById(R.id.et_id)
        et_pw = findViewById(R.id.et_pw)
        text_discorrect = findViewById(R.id.text_discorrect)
    }

    // id, pw입력
    private fun checkInput(){
        et_id.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            //text 입력중
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userId = et_id.text.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        et_pw.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            //text 입력중
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userPw = et_pw.text.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // 자동로그인
    private fun autoLogin() {
        et_id.setText(AppPref.prefs.myId)
        et_pw.setText(AppPref.prefs.myPw)

        // SharedPreferences 안에 값이 저장되어 있을 때 -> MainActivity로 이동
        if(!(AppPref.prefs.myId.isNullOrBlank()
            || AppPref.prefs.myPw.isNullOrBlank())) {
            val userInfo = UserInfo(AppPref.prefs.myId!!, AppPref.prefs.myPw!!)
            postLogin(userInfo)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    //-----------------------------------------------------------------------
    //                             Google Login
    //-----------------------------------------------------------------------
    //구글 세팅
    private fun googleBuildIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("500159069581-m2dqev5jhbpumksnoodl7bmi90v5kjtl.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    //구글 로그인 세팅
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

                val idTokenClass = IdToken(id_token = idToken)

                postGoogle(idTokenClass, email!!)
            }

        }catch (e: ApiException){
            Log.d(TAG, "Google sign in failed", e)
        }
    }

    //구글 로그인
    private fun GoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    //-----------------------------------------------------------------------
    //                            Rest Api function
    //-----------------------------------------------------------------------

    private fun postGoogle(idToken: IdToken, email: String) {
        RetrofitBuild.api.postGoogle(idToken).enqueue(object : Callback<ResultAndToken>{
            override fun onResponse(call: Call<ResultAndToken>, response: Response<ResultAndToken>) {
                if(response.isSuccessful) {
                    Log.d("$TAG2 - postGoogle", "연결성공")
                    val responseApi = response.body()!!
                    Log.d("$TAG2 - postGoogle",responseApi.toString())
                    val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                    mainIntent.putExtra("access_token", responseApi.access_token)
                    mainIntent.putExtra("refresh_token",responseApi.refresh_token)
                    mainIntent.putExtra("userId",email)
                    mainIntent.putExtra("userName",responseApi.name)
                    startActivity(mainIntent)
                } else {
                    val responseApi = response.body()
                    Log.d("$TAG2 - postGoogle",responseApi.toString())
                    Log.d("$TAG2 - postGoogle", "연결실패")
                }
            }
            override fun onFailure(call: Call<ResultAndToken>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d("$TAG2 - postGoogle", "인터넷 네트워크 문제")
                Log.d("$TAG2 - postGoogle", t.toString())
            }
        })
    }

    private fun postLogin(userInfo: UserInfo) {
        RetrofitBuild.api.postLogin(userInfo).enqueue(object : Callback<ResultAndToken>{
            override fun onResponse(call: Call<ResultAndToken>, response: Response<ResultAndToken>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d("$TAG2 - postLogin", "연결성공")
                    val responseApi = response.body()
                    Log.d("$TAG2 - postLogin",responseApi.toString())
                    if (responseApi != null) {
                        if (responseApi.result) {
                            Log.d("$TAG2 - postLogin", "로그인 성공")
                            AppPref.prefs.myId = userId
                            AppPref.prefs.myPw = userPw
                            val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                            mainIntent.putExtra("access_token", responseApi.access_token)
                            mainIntent.putExtra("refresh_token",responseApi.refresh_token)
                            mainIntent.putExtra("userId",userId)
                            mainIntent.putExtra("userName",responseApi.name)
                            startActivity(mainIntent)
                        }
                    }
                } else {
                    val errorResponse: ErrorResult? = gson.fromJson(response.errorBody()!!.charStream(), type)
                    Log.d("$TAG2 - postLogin", errorResponse.toString())
                    Log.d("$TAG2 - postLogin", "연결실패")
                    when(errorResponse!!.code){
                        40007 -> text_discorrect.text = "회원정보가 존재하지 않습니다."
                        40008 -> text_discorrect.text = "아이디/비밀번호가 일치하지 않습니다."
                    }
                }
            }
            override fun onFailure(call: Call<ResultAndToken>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d("$TAG2 - postLogin", "인터넷 네트워크 문제")
                Log.d("$TAG2 - postLogin", t.toString())
                text_discorrect.text = "네트워크 문제가 발생했습니다."
            }
        })
    }
}
