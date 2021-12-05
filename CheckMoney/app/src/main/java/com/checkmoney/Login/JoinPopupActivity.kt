package com.checkmoney.Login

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.CountDownTimer
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.checkmoney.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JoinPopupActivity(context : Context): Dialog(context) {
    private val dlg = Dialog(context)
    private val gson = Gson()
    private val type = object : TypeToken<ErrorResult>() {}.type

    private lateinit var btn_auth: Button
    private lateinit var btn_authCheck: Button
    private lateinit var btn_create: Button
    private lateinit var btn_cancle: Button
    private lateinit var et_email: EditText
    private lateinit var et_name: EditText
    private lateinit var et_pw: EditText
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
    private var userName = ""
    private var auth_count = 0
    private var name_count = 0
    private var pw_count = 0
    private val TAG = "JoinPopupActivity"
    private val TAG2 = "JoinPopupActivity_API"

    fun start() {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(R.layout.activity_join_popup)     //다이얼로그에 사용할 xml 파일을 불러옴
        dlg.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dlg.setCancelable(false)

        // 변수초기화
        setVariable()
        // 비밀번호 조건 체크
        pw_check()
        // 이름 적었나 체크
        name_check()

        btn_auth.setOnClickListener{
            auth_count = 0
            userEmail = et_email.text.toString()
            val email = Email(userEmail)
            postEmail(email)
        }

        btn_authCheck.setOnClickListener{
            userAuthNum = et_authNum.text.toString()
            val auth = AuthConfirm(userAuthNum, userEmail)
            Log.d(TAG,auth.email)
            postAuth(auth)
        }

        btn_create.setOnClickListener{
            userJoin()
        }

        btn_cancle.setOnClickListener{
            dlg.dismiss()
        }

        dlg.show()
    }

    private fun setVariable() {
        btn_auth = dlg.findViewById(R.id.btn_auth)
        btn_authCheck = dlg.findViewById(R.id.btn_auth_check)
        btn_create = dlg.findViewById(R.id.btn_create)
        btn_cancle = dlg.findViewById(R.id.btn_cancel)
        et_email = dlg.findViewById(R.id.et_id)
        et_name = dlg.findViewById(R.id.et_name)
        et_pw = dlg.findViewById(R.id.et_password)
        et_pwConfirm = dlg.findViewById(R.id.et_password_check)
        et_authNum = dlg.findViewById(R.id.et_auth)
        text_authMessage = dlg.findViewById(R.id.text_auth_message)
        text_authResult = dlg.findViewById(R.id.text_auth_result)
        text_timer = dlg.findViewById(R.id.text_timer)
        text_pwRegular = dlg.findViewById(R.id.text_pw_regular)
        text_pwConfirmCheck = dlg.findViewById(R.id.text_pw_confirm_result)
        text_JoinCheck = dlg.findViewById(R.id.text_join_check)
    }

    // 비밀번호 조건 체크
    private fun pw_check() {
        et_pw.addTextChangedListener(object:TextWatcher {
            var pw_first = et_pw.text.toString()
            var pw_check = et_pwConfirm.text.toString()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pw_first = et_pw.text.toString()
                pw_check = et_pwConfirm.text.toString()
                if(pw_first != ""){
                    val regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$".toRegex()
                    if (!regex.containsMatchIn(pw_first)){
                        text_pwRegular.text = "영문+숫자+특수문자를 포함하여 8자리 이상을 입력해 주세요."
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
                                context,
                                R.color.logoBlue
                            )
                        )
                        text_pwConfirmCheck.text = "비밀번호가 일치합니다."
                        pw_count = 1
                        userPassword = pw_first
                    } else {
                        text_pwConfirmCheck.setTextColor(ContextCompat.getColor(context,
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
            var pw_first = et_pw.text.toString()
            var pw_check = et_pwConfirm.text.toString()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pw_first = et_pw.text.toString()
                pw_check = et_pwConfirm.text.toString()
                if(pw_check != "") {
                    if (pw_first == pw_check) {
                        text_pwConfirmCheck.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.logoBlue
                            )
                        )
                        text_pwConfirmCheck.text = "비밀번호가 일치합니다."
                        pw_count = 1
                        userPassword = pw_first
                    } else {
                        text_pwConfirmCheck.setTextColor(ContextCompat.getColor(context,
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

    // 이름 적었나 체크
    private fun name_check(){
        et_name.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(et_name.text.toString() != ""){
                    name_count = 1
                    userName = et_name.text.toString()
                }
                else{
                    name_count = 0
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // 회원가입시 모두 입력했나 확인
    private fun userJoin() {
        if(auth_count == 1 && pw_count == 1 && name_count == 1){
            Log.d(TAG, "name : $userName")
            val joinInfo = Join(userEmail,userPassword,userName)
            postJoin(joinInfo)
            dlg.dismiss()
        }
        else{
            text_JoinCheck.text="다시 한번 확인하여 주십시오."
        }
    }

    // 인증시간
    private fun countDownTimer(){
        object:CountDownTimer(300000,1){
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                if(auth_count == 0) {
                    val time = millisUntilFinished / 1000
                    val min = time / 60
                    val sec = time % 60
                    text_timer.text = "%02d".format(min) + ":" + "%02d".format(sec)
                }
                else{
                    text_timer.text = ""
                }
            }

            override fun onFinish() {
                if(auth_count == 0) {
                    text_timer.text = "시간만료"
                }
                else{
                    text_timer.text = ""
                }
            }
        }.start()
    }

    //-----------------------------------------------------------------------
    //                           Rest Api function
    //-----------------------------------------------------------------------
    private fun postEmail(email: Email) {
        RetrofitBuild.api.postEmail(email).enqueue(object : Callback<Result>{
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) { // response 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG,responseApi.toString())
                    text_authMessage.setTextColor(ContextCompat.getColor(context, R.color.logoBlue))
                    text_authMessage.text = "인증번호전송"
                    countDownTimer()
                } else { // response 400
                    val errorResponse: ErrorResult? = gson.fromJson(response.errorBody()!!.charStream(), type)
                    Log.d(TAG2, errorResponse.toString())
                    Log.d(TAG2, "연결실패")
                    text_authMessage.setTextColor(ContextCompat.getColor(context, R.color.red))
                    when(errorResponse!!.code){
                        40001 -> text_authMessage.text = "탈퇴한 계정입니다. 다음에 다시 가입해 주십시오."
                        40002 -> text_authMessage.text = "이미 사용중인 계정입니다."
                        50000 -> text_authMessage.text = "잘못된 주소를 입력하였습니다."
                    }
                }
            }
            override fun onFailure(call: Call<Result>, t: Throwable) { // response 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
                text_authResult.setTextColor(ContextCompat.getColor(context, R.color.red))
                text_authResult.text = "네트워크 문제가 발생하였습니다."
            }
        })
    }

    private fun postAuth(authConfirm: AuthConfirm) {
        RetrofitBuild.api.postAuth(authConfirm).enqueue(object : Callback<Result>{
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) { // response 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,response.toString())
                    if (responseApi != null) {
                        if(responseApi.result){
                            text_authResult.setTextColor(ContextCompat.getColor(context,
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
                    text_authResult.setTextColor(ContextCompat.getColor(context, R.color.red))
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
                text_authResult.setTextColor(ContextCompat.getColor(context, R.color.red))
                text_authResult.text = "네트워크 문제가 발생하였습니다."
            }
        })
    }

    private fun postJoin(join: Join){
        RetrofitBuild.api.postJoin(join).enqueue(object : Callback<Result>{
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) { // response 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                } else { // response 400
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                    Log.d(TAG2, "연결실패")
                }
            }
            override fun onFailure(call: Call<Result>, t: Throwable) { // response 500
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }
}