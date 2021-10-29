package com.checkmoney

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
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
    private lateinit var pw: EditText
    private lateinit var pwConfirm: EditText
    private lateinit var authNum: EditText
    private lateinit var authMessage: TextView
    private lateinit var authResult: TextView
    private lateinit var pwConfirmCheck: TextView

    private lateinit var userEmail: String
    private lateinit var userAuthNum: String
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
        pw = dlg.findViewById(R.id.et_password)
        pwConfirm = dlg.findViewById(R.id.et_password_check)
        authNum = dlg.findViewById(R.id.et_auth)
        authMessage = dlg.findViewById(R.id.text_auth_message)
        authResult = dlg.findViewById(R.id.text_auth_result)
        pwConfirmCheck = dlg.findViewById(R.id.text_pw_confirm_result)

        auth.setOnClickListener{
            userEmail = email.text.toString()
            val email = Email(email = email.text.toString())
            postEmail(email)
        }

        authCheck.setOnClickListener{
            userAuthNum = authNum.text.toString()
            val auth = AuthConfirm(userAuthNum, userEmail)
            Log.d(TAG,auth.email)
            postAuth(auth)
        }

        create.setOnClickListener{
            //TODO 아이디 생성
            dlg.dismiss()
        }

        cancle.setOnClickListener{
            dlg.dismiss()
        }

        dlg.show()

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
}