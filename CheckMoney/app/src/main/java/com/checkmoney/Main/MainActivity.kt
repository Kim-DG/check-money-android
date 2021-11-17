package com.checkmoney

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.checkmoney.Login.JoinPopupActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var mBackWait:Long = 0

    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var layout_drawer: DrawerLayout
    private lateinit var nav_header: View
    private lateinit var rv_profile: RecyclerView
    private lateinit var btn_navi: ImageView
    private lateinit var btn_logout: TextView
    private lateinit var naviView: NavigationView
    private lateinit var text_email: TextView

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var access_token: String
    private lateinit var refresh_token: String
    private lateinit var user_email: String
    private lateinit var bearerAccessToken: String

    private val TAG = "MainActivity"
    private val TAG2 = "MainActivity_API"
    private var accountId = -1
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //변수 초기화
        setVariable()
        //drawer layout 사이즈 조절
        setLayoutSize()
        //구글 세팅
        googleBuildIn()
        //access token, refresh token, 사용자 이메일을 LoginActivity에서 받아옴
        getExtraLogin()

        getAccount(bearerAccessToken)

        btn_navi.setOnClickListener {
            layout_drawer.openDrawer(GravityCompat.START)
            profileAdapter.datas = ProfileDataList.datas
            profileAdapter.notifyDataSetChanged()
        }

        btn_logout.setOnClickListener {
            AppPref.prefs.clearUser(this)
            googleSignOut()
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }
    }

    //변수 초기화
    @SuppressLint("CutPasteId")
    private fun setVariable() {
        layout_drawer = findViewById(R.id.layout_drawer)
        btn_navi = findViewById(R.id.btn_navi)
        naviView = findViewById(R.id.naviView)
        nav_header = naviView.getHeaderView(0)
        rv_profile = nav_header.findViewById(R.id.rv_profile)
        btn_logout = nav_header.findViewById(R.id.text_logout)
        text_email = nav_header.findViewById(R.id.text_email)

        naviView.setNavigationItemSelectedListener(this)// 네비게이션 메뉴 아이템에 클릭 속성 부여
    }

    //drawer layout 사이즈 조절
    private fun setLayoutSize() {
        val display = windowManager.defaultDisplay // in case of Activity
        val size = Point()
        display.getRealSize(size) // or getSize(size)
        val width = size.x * (0.66)
        val height = size.y * (0.66)
        nav_header.layoutParams.height = height.toInt()
        naviView.layoutParams.width= width.toInt()
    }

    //access token, refresh token, 사용자 이메일을 LoginActivity에서 받아옴
    private fun getExtraLogin() {
        val intent = getIntent()
        access_token = intent.getStringExtra("access_token")!!
        refresh_token = intent.getStringExtra("refresh_token")!!
        user_email = intent.getStringExtra("userId")!!
        text_email.text = user_email
        bearerAccessToken = "Bearer $access_token"
    }

    //recycler항목 추가
    @SuppressLint("NotifyDataSetChanged")
    private fun initRecycler(accountList: ArrayList<AccountModel>) {
        profileAdapter = ProfileAdapter(this,access_token,refresh_token,user_email,accountId)
        rv_profile.adapter = profileAdapter

        ProfileDataList.datas.apply {
            accountList.forEach {
                add(ProfileData(title = it.title, description = it.description, id = it.id))
            }
            Log.d(TAG,"Profile Data list" + ProfileDataList.datas.toString())
            profileAdapter.datas = ProfileDataList.datas
            profileAdapter.notifyDataSetChanged()
        }
    }

    // 네비게이션 메뉴 아이템 클릭 시 수행
    @SuppressLint("NotifyDataSetChanged")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.add -> {
                val dlg = Dialog(this@MainActivity)
                dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
                dlg.setContentView(R.layout.wallet_create_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴
                dlg.show()

                val et_wname = dlg.findViewById<EditText>(R.id.et_wname)
                val et_description = dlg.findViewById<EditText>(R.id.et_description)
                val btn_create = dlg.findViewById<Button>(R.id.btn_create)
                val btn_cancle = dlg.findViewById<Button>(R.id.btn_cancel)

                btn_create.setOnClickListener {
                    val account = Account(title = et_wname?.text.toString(), description = et_description?.text.toString())
                    postAccount(bearerAccessToken, account)
                    dlg.dismiss()
                }
                btn_cancle.setOnClickListener {
                    dlg.dismiss()
                }
            }
            R.id.test2 -> Toast.makeText(applicationContext, "test2", Toast.LENGTH_SHORT).show()
            R.id.test3 -> Toast.makeText(applicationContext, "test3", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    //뒤로가기 두번클릭 시 앱 종료
    override fun onBackPressed() {
        if (System.currentTimeMillis() - mBackWait >= 2000) {
            mBackWait = System.currentTimeMillis()
            Toast.makeText(this, " 한 번 더 누르면 종료됩니다.", Toast.LENGTH_LONG).show()
        } else {
            finishAffinity()
            System.runFinalization()
            System.exit(0)
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
    //구글이메일 로그아웃
    private fun googleSignOut() { // 로그아웃
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

    private fun getAccount(accessToken: String) {
        RetrofitBuild.api.getAccount(accessToken).enqueue(object : Callback<ResultAccountList> {
            override fun onResponse(call: Call<ResultAccountList>, response: Response<ResultAccountList>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                    initRecycler(response.body()!!.rows)
                } else { // code == 400
                    Log.d(TAG2, "연결실패")
                }
            }
            override fun onFailure(call: Call<ResultAccountList>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }

    private fun postAccount(accessToken: String, account: Account) {
        RetrofitBuild.api.postAccount(accessToken, account).enqueue(object : Callback<ResultAccount> {
            override fun onResponse(call: Call<ResultAccount>, response: Response<ResultAccount>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                    ProfileDataList.datas.apply {
                        add(ProfileData(title = account.title, description = account.description ,id = responseApi!!.id))
                        profileAdapter.datas = ProfileDataList.datas
                        profileAdapter.notifyDataSetChanged()
                    }
                } else { // code == 400
                    Log.d(TAG2, "연결실패")
                }
            }
            override fun onFailure(call: Call<ResultAccount>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }
}