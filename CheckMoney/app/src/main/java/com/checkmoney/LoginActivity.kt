package com.checkmoney

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
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
    private val RC_SIGN_IN = 99
    private val TAG = "LoginActivity"
    private val TAG2 = "LoginActivity_API"
    private lateinit var btn_join: Button
    private lateinit var btn_login: Button
    private lateinit var btn_signInButton: SignInButton
    private lateinit var et_id: EditText
    private lateinit var et_pw: EditText
    private lateinit var userId: String
    private lateinit var userPw: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_join = findViewById(R.id.btn_join)
        btn_login = findViewById(R.id.btn_login)
        btn_signInButton = findViewById(R.id.sign_in_button)
        et_id = findViewById(R.id.et_id)
        et_pw = findViewById(R.id.et_pw)

        checkInput()

        btn_join.setOnClickListener{
            val dialog = JoinPopupActivity(this@LoginActivity)
            dialog.start()
        }

        btn_login.setOnClickListener {
            if(userId != "" && userPw != ""){
                val userInfo = UserInfo(userId,userPw)
                postLogin(userInfo)
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
        googleBuildIn()
    }

    private fun googleBuildIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("500159069581-m2dqev5jhbpumksnoodl7bmi90v5kjtl.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

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

    private fun updateUI() {
        val Intent = Intent(this, MainActivity::class.java)
        startActivity(Intent)
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
    //----------------------------Rest Api function--------------------------
    //-----------------------------------------------------------------------

    private fun postgoogle(idToken: IdToken) {
        RetrofitBuild.api.postGoogle(idToken).enqueue(object : Callback<IdToken>{
            override fun onResponse(call: Call<IdToken>, response: Response<IdToken>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val a: IdToken = response.body()!!
                    Log.d(TAG2,a.toString())
                } else { // code == 400
                    // 실패 처리
                    Log.d(TAG2, "연결실패")
                }
            }
            override fun onFailure(call: Call<IdToken>, t: Throwable) { // code == 500
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
                    val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(mainIntent)
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


}
