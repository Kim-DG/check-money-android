package com.checkmoney

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JoinPopupActivity(context : Context): Dialog(context) {
    private val dlg = Dialog(context)
    private lateinit var auth: Button
    private lateinit var authCheck: Button
    private lateinit var create: Button
    private lateinit var cancle: Button
    private lateinit var email: EditText
    private lateinit var name: EditText
    private lateinit var pw: EditText
    private lateinit var pwConfirm: EditText
    private lateinit var authNum: EditText
    private lateinit var authMessage: TextView
    private lateinit var authResult: TextView
    private lateinit var pwConfirmCheck: TextView
    private lateinit var textJoinCheck: TextView

    private lateinit var userPassword: String
    private lateinit var userEmail: String
    private lateinit var userAuthNum: String
    private lateinit var userName: String
    private var auth_count = 0
    private var name_count = 0
    private var pw_count = 0
    private val TAG = "JoinPopupActivity"

    fun start() {
        //context.dialogResize(this@JoinPopupActivity, 0.9f, 0.9f)
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(R.layout.activity_join_popup)     //다이얼로그에 사용할 xml 파일을 불러옴
        dlg.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)
        dlg.setCancelable(false)

        auth = dlg.findViewById(R.id.btn_auth)
        authCheck = dlg.findViewById(R.id.btn_auth_check)
        create = dlg.findViewById(R.id.btn_create)
        cancle = dlg.findViewById(R.id.btn_cancel)
        email = dlg.findViewById(R.id.et_id)
        name = dlg.findViewById(R.id.et_name)
        pw = dlg.findViewById(R.id.et_password)
        pwConfirm = dlg.findViewById(R.id.et_password_check)
        authNum = dlg.findViewById(R.id.et_auth)
        authMessage = dlg.findViewById(R.id.text_auth_message)
        authResult = dlg.findViewById(R.id.text_auth_result)
        pwConfirmCheck = dlg.findViewById(R.id.text_pw_confirm_result)
        textJoinCheck = dlg.findViewById(R.id.text_join_check)

        pw_check()
        name_check()

        auth.setOnClickListener{
            auth_count = 0
            userEmail = email.text.toString()
            val email = Email(userEmail)
            postEmail(email)
        }

        authCheck.setOnClickListener{
            userAuthNum = authNum.text.toString()
            val auth = AuthConfirm(userAuthNum, userEmail)
            Log.d(TAG,auth.email)
            postAuth(auth)
        }

        create.setOnClickListener{
            userJoin()
        }

        cancle.setOnClickListener{
            dlg.dismiss()
        }

        dlg.show()
    }

    private fun userJoin() {
        if(auth_count == 1 && pw_count == 1 && name_count == 1){
            Log.d(TAG, "name : $userName")
            val joinInfo = Join(userEmail,userPassword,userName)
            postJoin(joinInfo)
            dlg.dismiss()
        }
        else{
            textJoinCheck.text="다시 한번 확인하여 주십시오."
        }
    }

    private fun name_check(){
        name.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(name.text.toString() != ""){
                    name_count = 1
                    userName = name.text.toString()
                }
                else{
                    name_count = 0
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun pw_check() {
        pw.addTextChangedListener(object:TextWatcher {
            var pw_first = pw.text.toString()
            var pw_check = pwConfirm.text.toString()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pw_first = pw.text.toString()
                pw_check = pwConfirm.text.toString()
                if(pw_check != "") {
                    if (pw_first == pw_check) {
                        pwConfirmCheck.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.logoBlue
                            )
                        )
                        pwConfirmCheck.text = "비밀번호가 일치합니다."
                        pw_count = 1
                        userPassword = pw_first
                    } else {
                        pwConfirmCheck.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.red
                            )
                        )
                        pwConfirmCheck.text = "비밀번호가 일치하지 않습니다."
                        pw_count = 0
                    }
                }
                else {
                    pwConfirmCheck.text = ""
                    pw_count = 0
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        pwConfirm.addTextChangedListener(object:TextWatcher {
            var pw_first = pw.text.toString()
            var pw_check = pwConfirm.text.toString()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pw_first = pw.text.toString()
                pw_check = pwConfirm.text.toString()
                if(pw_check != "") {
                    if (pw_first == pw_check) {
                        pwConfirmCheck.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.logoBlue
                            )
                        )
                        pwConfirmCheck.text = "비밀번호가 일치합니다."
                        pw_count = 1
                        userPassword = pw_first
                    } else {
                        pwConfirmCheck.setTextColor(ContextCompat.getColor(context, R.color.red))
                        pwConfirmCheck.text = "비밀번호가 일치하지 않습니다."
                        pw_count = 0
                    }
                }
                else {
                    pwConfirmCheck.text = ""
                    pw_count = 0
                }

            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    //-----------------------------------------------------------------------
    //----------------------------Rest Api function--------------------------
    //-----------------------------------------------------------------------
    private fun postEmail(email: Email) {
        RetrofitBuild.api.postEmail(email).enqueue(object : Callback<Result>{
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG, "연결성공")
                    val a = response.body()
                    Log.d(TAG,a.toString())
                    authResult.setTextColor(ContextCompat.getColor(context, R.color.logoBlue))
                    authMessage.text = "인증번호전송"
                } else { // code == 400
                    // 실패 처리
                    Log.d(TAG, "연결실패")
                    authResult.setTextColor(ContextCompat.getColor(context, R.color.red))
                    authMessage.text = "잘못된 주소를 입력하였습니다."
                }
            }
            override fun onFailure(call: Call<Result>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG, "인터넷 네트워크 문제")
                Log.d(TAG, t.toString())
                authResult.setTextColor(ContextCompat.getColor(context, R.color.red))
                authResult.text = "네트워크 문제가 발생하였습니다."
            }
        })
    }

    private fun postAuth(authConfirm: AuthConfirm) {
        RetrofitBuild.api.postAuth(authConfirm).enqueue(object : Callback<Result>{
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG, "연결성공")
                    val result = response.body()
                    Log.d(TAG,result.toString())
                    if (result != null) {
                        if(result.result == "true"){
                            authResult.setTextColor(ContextCompat.getColor(context, R.color.logoBlue))
                            authResult.text = "인증성공"
                            auth_count = 1
                        }
                    }
                } else { // code == 400
                    Log.d(TAG, "연결실패")
                    authResult.setTextColor(ContextCompat.getColor(context, R.color.red))
                    authResult.text = "인증번호 불일치"
                }
            }
            override fun onFailure(call: Call<Result>, t: Throwable) { // code == 500
                Log.d(TAG, "인터넷 네트워크 문제")
                Log.d(TAG, t.toString())
                authResult.setTextColor(ContextCompat.getColor(context, R.color.red))
                authResult.text = "네트워크 문제가 발생하였습니다."
            }
        })
    }

    private fun postJoin(join: Join){
        RetrofitBuild.api.postJoin(join).enqueue(object : Callback<Result>{
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) { // <--> response.code == 201
                    Log.d(TAG, "연결성공")
                    val result = response.body()
                    Log.d(TAG,result.toString())
                } else { // code == 400
                    Log.d(TAG, "연결실패")
                }
            }
            override fun onFailure(call: Call<Result>, t: Throwable) { // code == 500
                Log.d(TAG, "인터넷 네트워크 문제")
                Log.d(TAG, t.toString())
            }
        })
    }
}