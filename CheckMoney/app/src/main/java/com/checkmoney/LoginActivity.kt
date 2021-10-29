package com.checkmoney

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
    private lateinit var join: Button
    private lateinit var login: Button
    private lateinit var signInButton: SignInButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        join = findViewById(R.id.btn_join)
        login = findViewById(R.id.btn_login)
        signInButton = findViewById(R.id.sign_in_button)

        join.setOnClickListener{

            val dialog = JoinPopupActivity(this@LoginActivity)
            dialog.start()/*
            val joinIntent = Intent(this,JoinPopupActivity::class.java)
            startActivity(joinIntent)*/
        }

        signInButton.setOnClickListener {
            val gsa = GoogleSignIn.getLastSignedInAccount(this@LoginActivity)
            if (gsa?.id != null) {
                val Intent = Intent(this, MainActivity::class.java)
                startActivity(Intent)
            }
            else signIn()
        }

        googleBuildIn()

        val testEmail = "kdg960914@naver.com"
        val test = Email(email = testEmail)
        //test()
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

    private fun postEmail(email: Email) {
        RetrofitBuild.api.postEmail(email).enqueue(object : Callback<Result>{
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val a = response.body()
                    Log.d(TAG2,a.toString())
                } else { // code == 400
                    // 실패 처리
                    Log.d(TAG2, "연결실패")
                }
            }
            override fun onFailure(call: Call<Result>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }


}
