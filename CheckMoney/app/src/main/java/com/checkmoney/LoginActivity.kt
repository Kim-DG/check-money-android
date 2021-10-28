package com.checkmoney

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("500159069581-m2dqev5jhbpumksnoodl7bmi90v5kjtl.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val gsa = GoogleSignIn.getLastSignedInAccount(this@LoginActivity)

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        val testEmail = "kdg960914@naver.com"
        postEmail(testEmail)
        //test()

        val signInButton = findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.setOnClickListener {
            signIn()
        }
        if (gsa?.id != null) {
            val Intent = Intent(this, MainActivity::class.java)
            startActivity(Intent)
        }
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

                postgoogle(idToken)
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

    private fun postgoogle(idToken: String) {
        RetrofitBuild.api.postGoogle(idToken).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val a: String = response.body()!!
                    Log.d(TAG2,a)
                } else { // code == 400
                    // 실패 처리
                    Log.d(TAG2, "연결실패")
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }

    private fun postEmail(email: String) {
        RetrofitBuild.api.postEmail(email).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val a: String = response.body()!!
                    Log.d(TAG2,a)
                } else { // code == 400
                    // 실패 처리
                    Log.d(TAG2, "연결실패")
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }

    private fun test(){
        RetrofitBuild.api.testApi().enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val a: String = response.body()!!
                    Log.d(TAG2,a)
                } else { // code == 400
                    // 실패 처리
                    Log.d(TAG2, "연결실패")
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }
}
