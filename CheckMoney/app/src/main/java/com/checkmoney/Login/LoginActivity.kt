package com.checkmoney

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.checkmoney.Login.JoinPopupActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
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
    private lateinit var btn_auth: Button
    private lateinit var btn_authCheck: Button
    private lateinit var btn_create: Button
    private lateinit var btn_cancle: Button
    private lateinit var et_email: EditText
    private lateinit var et_newPw: EditText
    private lateinit var et_pwConfirm: EditText
    private lateinit var et_authNum: EditText
    private lateinit var text_authMessage: TextView
    private lateinit var text_authResult: TextView
    private lateinit var text_timer: TextView
    private lateinit var text_pwRegular: TextView
    private lateinit var text_pwConfirmCheck: TextView
    private lateinit var text_JoinCheck: TextView

    private var userPassword = ""
    private var userEmail = ""
    private var userAuthNum = ""
    private var auth_count = 0
    private var pw_count = 0
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

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            tokens.push_token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, tokens.push_token)
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })

        // 회원가입
        btn_join.setOnClickListener{
            val dialog = JoinPopupActivity(this@LoginActivity)
            dialog.start()
        }

        val gsa = GoogleSignIn.getLastSignedInAccount(this@LoginActivity)
        if (gsa?.id != null) {
            val idTokenClass = IdToken(id_token = gsa.idToken, push_token = tokens.push_token)
            Log.d("TAG",gsa.email!!)
            postGoogle(idTokenClass, gsa.email!!)
        }

        btn_login.setOnClickListener {
            if(userId != "" && userPw != ""){
                val userInfo = UserInfo(userId,userPw,tokens.push_token)
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
            findPassword()
        }
    }

    private fun findPassword() {
        val dlg = Dialog(this@LoginActivity)
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(R.layout.dialog_find_pwd)     //다이얼로그에 사용할 xml 파일을 불러옴
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dlg.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dlg.setCancelable(false)

        btn_auth = dlg.findViewById(R.id.btn_auth)
        btn_authCheck = dlg.findViewById(R.id.btn_auth_check)
        btn_create = dlg.findViewById(R.id.btn_create)
        btn_cancle = dlg.findViewById(R.id.btn_cancel)
        et_email = dlg.findViewById(R.id.et_id)
        et_newPw = dlg.findViewById(R.id.et_password)
        et_pwConfirm = dlg.findViewById(R.id.et_password_check)
        et_authNum = dlg.findViewById(R.id.et_auth)
        text_authMessage = dlg.findViewById(R.id.text_auth_message)
        text_authResult = dlg.findViewById(R.id.text_auth_result)
        text_timer = dlg.findViewById(R.id.text_timer)
        text_pwRegular = dlg.findViewById(R.id.text_pw_regular)
        text_pwConfirmCheck = dlg.findViewById(R.id.text_pw_confirm_result)
        text_JoinCheck = dlg.findViewById(R.id.text_join_check)

        pw_check(dlg)

        btn_auth.setOnClickListener{
            auth_count = 0
            userEmail = et_email.text.toString()
            val email = Email(userEmail)
            postEmailForPwd(email,dlg)
        }

        btn_authCheck.setOnClickListener{
            userAuthNum = et_authNum.text.toString()
            val auth = AuthConfirm(userAuthNum, userEmail)
            Log.d(TAG,auth.email)
            postAuth(auth,dlg)
        }

        btn_create.setOnClickListener{
            findPwd(dlg)
        }

        btn_cancle.setOnClickListener{
            dlg.dismiss()
        }

        dlg.show()
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
            val userInfo = UserInfo(AppPref.prefs.myId!!, AppPref.prefs.myPw!!,tokens.push_token)
            postLogin(userInfo)
        }
    }

    // 비밀번호 조건 체크
    private fun pw_check(dlg: Dialog) {
        et_newPw.addTextChangedListener(object:TextWatcher {
            var pw_first = et_newPw.text.toString()
            var pw_check = et_pwConfirm.text.toString()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pw_first = et_newPw.text.toString()
                pw_check = et_pwConfirm.text.toString()
                if(pw_first != ""){
                    val regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$".toRegex()
                    if (!regex.containsMatchIn(pw_first)){
                        text_pwRegular.text = "영문+숫자+특수문자를 포함하여 8자리 이상을 입력해 주세요."
                        pw_count = 0
                    }
                    else{
                        text_pwRegular.text = ""
                    }
                }
                else{
                    text_pwRegular.text = ""
                }
                if(pw_check != "") {
                    if (pw_first == pw_check) {
                        text_pwConfirmCheck.setTextColor(
                            ContextCompat.getColor(
                                dlg.context,
                                R.color.logoBlue
                            )
                        )
                        text_pwConfirmCheck.text = "비밀번호가 일치합니다."
                        pw_count = 1
                        userPassword = pw_first
                    } else {
                        text_pwConfirmCheck.setTextColor(ContextCompat.getColor(dlg.context,
                            R.color.red
                        ))
                        text_pwConfirmCheck.text = "비밀번호가 일치하지 않습니다."
                        pw_count = 0
                    }
                }
                else {
                    text_pwConfirmCheck.text = ""
                    pw_count = 0
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        et_pwConfirm.addTextChangedListener(object:TextWatcher {
            var pw_first = et_newPw.text.toString()
            var pw_check = et_pwConfirm.text.toString()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pw_first = et_newPw.text.toString()
                pw_check = et_pwConfirm.text.toString()
                if(pw_check != "") {
                    if (pw_first == pw_check) {
                        text_pwConfirmCheck.setTextColor(
                            ContextCompat.getColor(
                                dlg.context,
                                R.color.logoBlue
                            )
                        )
                        text_pwConfirmCheck.text = "비밀번호가 일치합니다."
                        pw_count = 1
                        userPassword = pw_first
                    } else {
                        text_pwConfirmCheck.setTextColor(ContextCompat.getColor(dlg.context,
                            R.color.red
                        ))
                        text_pwConfirmCheck.text = "비밀번호가 일치하지 않습니다."
                        pw_count = 0
                    }
                }
                else {
                    text_pwConfirmCheck.text = ""
                    pw_count = 0
                }

            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // 회원가입시 모두 입력했나 확인
    private fun findPwd(dlg: Dialog) {
        if(auth_count == 1 && pw_count == 1){
            val emailPwd = EmailPwd(userEmail,userPassword)
            postFindPwd(emailPwd,dlg)
            dlg.dismiss()
        }
        else{
            text_JoinCheck.text="다시 한번 확인하여 주십시오."
        }
    }

    // 인증시간
    private fun countDownTimer() {
        object : CountDownTimer(300000, 1) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                if (auth_count == 0) {
                    val time = millisUntilFinished / 1000
                    val min = time / 60
                    val sec = time % 60
                    text_timer.text = "%02d".format(min) + ":" + "%02d".format(sec)
                } else {
                    text_timer.text = ""
                }
            }

            override fun onFinish() {
                if (auth_count == 0) {
                    text_timer.text = "시간만료"
                } else {
                    text_timer.text = ""
                }
            }
        }.start()
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

                val idTokenClass = IdToken(id_token = idToken, tokens.push_token)

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
                    tokens.access_token = responseApi.access_token!!
                    tokens.refresh_token = responseApi.refresh_token!!

                    val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
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
                            tokens.access_token = responseApi.access_token!!
                            tokens.refresh_token = responseApi.refresh_token!!

                            val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
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

    private fun postEmailForPwd(email: Email,dlg: Dialog) {
        RetrofitBuild.api.postEmailForPwd(email).enqueue(object : Callback<Result>{
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) {
                    Log.d("$TAG2 - postGoogle", "연결성공")
                    val responseApi = response.body()!!
                    Log.d("$TAG2 - postGoogle",responseApi.toString())
                    text_authMessage.setTextColor(ContextCompat.getColor(dlg.context, R.color.logoBlue))
                    text_authMessage.text = "인증번호전송"
                    countDownTimer()
                } else {
                    val errorResponse: ErrorResult? = gson.fromJson(response.errorBody()!!.charStream(), type)
                    val responseApi = response.body()
                    Log.d("$TAG2 - postGoogle",responseApi.toString())
                    Log.d("$TAG2 - postGoogle", "연결실패")
                    text_authMessage.setTextColor(ContextCompat.getColor(dlg.context, R.color.red))
                    when(errorResponse!!.code){
                        40001 -> text_authMessage.text = "탈퇴한 계정입니다. 다음에 다시 가입해 주십시오."
                        40002 -> text_authMessage.text = "이미 사용중인 계정입니다."
                        50000 -> text_authMessage.text = "잘못된 주소를 입력하였습니다."
                    }
                }
            }
            override fun onFailure(call: Call<Result>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d("$TAG2 - postGoogle", "인터넷 네트워크 문제")
                Log.d("$TAG2 - postGoogle", t.toString())
            }
        })
    }

    private fun postFindPwd(emailPwd: EmailPwd,dlg: Dialog) {
        RetrofitBuild.api.postFindPwd(emailPwd).enqueue(object : Callback<Result>{
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) {
                    Log.d("$TAG2 - postGoogle", "연결성공")
                    val responseApi = response.body()!!
                    Log.d("$TAG2 - postGoogle",responseApi.toString())
                } else {
                    val responseApi = response.body()
                    Log.d("$TAG2 - postGoogle",responseApi.toString())
                    Log.d("$TAG2 - postGoogle", "연결실패")
                }
            }
            override fun onFailure(call: Call<Result>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d("$TAG2 - postGoogle", "인터넷 네트워크 문제")
                Log.d("$TAG2 - postGoogle", t.toString())
            }
        })
    }

    private fun postAuth(authConfirm: AuthConfirm, dlg: Dialog) {
        RetrofitBuild.api.postAuth(authConfirm).enqueue(object : Callback<Result>{
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) { // response 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,response.toString())
                    if (responseApi != null) {
                        if(responseApi.result){
                            text_authResult.setTextColor(
                                ContextCompat.getColor(dlg.context,
                                R.color.logoBlue
                            ))
                            text_authResult.text = "인증성공"
                            auth_count = 1
                        }
                    }
                } else { // response 400
                    val errorResponse: ErrorResult? = gson.fromJson(response.errorBody()!!.charStream(), type)
                    Log.d(TAG2, errorResponse.toString())
                    Log.d(TAG2, "연결실패")
                    text_authResult.setTextColor(ContextCompat.getColor(dlg.context, R.color.red))
                    when(errorResponse!!.code){
                        40003 -> text_authResult.text = "인증번호가 일치하지 않습니다."
                        40004 -> text_authResult.text = "인증시간을 초과하였습니다."
                        40005 -> text_authResult.text = "이메일을 입력한 후, 인증버튼을 눌러 주십시오."
                    }
                }
            }
            override fun onFailure(call: Call<Result>, t: Throwable) { // response 500
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
                text_authResult.setTextColor(ContextCompat.getColor(dlg.context, R.color.red))
                text_authResult.text = "네트워크 문제가 발생하였습니다."
            }
        })
    }
    companion object {
        @SuppressLint("StaticFieldLeak")
        @JvmField
        var context_login // context 변수 선언
                : Context? = null
    }
}



