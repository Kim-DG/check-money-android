package com.checkmoney

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Point
import android.graphics.Typeface
import android.graphics.fonts.FontFamily
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class WalletActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val money_datas = mutableListOf<MoneyProfileData>()
    private val datas = mutableListOf<ProfileData>()

    private lateinit var walletDatas: ProfileData
    private lateinit var money_profileAdapter: MoneyProfileAdapter
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var money_rv_profile: RecyclerView
    private lateinit var rv_profile: RecyclerView
    private lateinit var layout_drawer: DrawerLayout
    private lateinit var nav_header: View
    private lateinit var text_wname: TextView
    private lateinit var btn_logout: TextView
    private lateinit var btn_left: ImageView
    private lateinit var btn_right: ImageView
    private lateinit var btn_total: TextView
    private lateinit var btn_expense: TextView
    private lateinit var btn_income: TextView
    private lateinit var btn_navi: ImageView
    private lateinit var text_ym: TextView
    private lateinit var text_email: TextView
    private lateinit var naviView: NavigationView

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var access_token: String
    private lateinit var refresh_token: String
    private lateinit var user_email: String

    @SuppressLint("SimpleDateFormat")
    private var df = SimpleDateFormat("yyyy/MM")
    private var cal = Calendar.getInstance()
    private var listType = 0

    private val TOTAL = 0
    private val EXPENSE = 1
    private val INCOME = 2

    private val TAG = "WalletActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        //변수 초기화
        setVariable()
        //drawer layout 사이즈 조절
        setLayoutSize()
        //구글 세팅
        googleBuildIn()
        //access token, refresh token, 사용자 이메일을 LoginActivity에서 받아옴
        getExtraLogin()
        //recycler항목 추가
        initRecycler()
        //menu recyceler항목 추가
        menuRecycler()


        btn_left.setOnClickListener {
            if(cal.get(Calendar.MONTH) == 1) {
                cal.add(Calendar.MONTH, 11)
                cal.add(Calendar.YEAR, -1)
            }
            else{
                cal.add(Calendar.MONTH, -1)
            }
            text_ym.text = df.format(cal.time)
        }

        btn_right.setOnClickListener {
            if(cal.get(Calendar.MONTH) == 12) {
                cal.add(Calendar.MONTH, -11)
                cal.add(Calendar.YEAR, 1)
            }
            else{
                cal.add(Calendar.MONTH, 1)
            }
            text_ym.text = df.format(cal.time)
        }

        btn_total.setOnClickListener {
            btn_total.setTypeface(null, Typeface.BOLD)
            btn_expense.typeface = Typeface.DEFAULT
            btn_income.typeface = Typeface.DEFAULT
            listType = TOTAL
        }

        btn_expense.setOnClickListener {
            btn_total.typeface = Typeface.DEFAULT
            btn_expense.setTypeface(null, Typeface.BOLD)
            btn_income.typeface = Typeface.DEFAULT
            listType = EXPENSE
        }

        btn_income.setOnClickListener {
            btn_total.typeface = Typeface.DEFAULT
            btn_expense.typeface = Typeface.DEFAULT
            btn_income.setTypeface(null, Typeface.BOLD)
            listType = INCOME
        }

        btn_navi.setOnClickListener {
            layout_drawer.openDrawer(GravityCompat.START)
        }

        btn_logout.setOnClickListener {
            AppPref.prefs.clearUser(this)
            googleSignOut()
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }
    }

    private fun setVariable() {
        layout_drawer = findViewById(R.id.layout_drawer)
        naviView = findViewById(R.id.naviView)
        nav_header = naviView.getHeaderView(0)
        text_wname = findViewById(R.id.text_wname)
        money_rv_profile = findViewById(R.id.rv_profile)
        rv_profile = nav_header.findViewById(R.id.rv_profile)
        btn_logout = nav_header.findViewById(R.id.text_logout)
        btn_left = findViewById(R.id.btn_left)
        btn_right = findViewById(R.id.btn_right)
        btn_total = findViewById(R.id.btn_total)
        btn_expense = findViewById(R.id.btn_expense)
        btn_income = findViewById(R.id.btn_income)
        btn_navi = findViewById(R.id.btn_navi)
        text_ym = findViewById(R.id.text_ym)
        text_email = nav_header.findViewById(R.id.text_email)

        walletDatas = intent.getParcelableExtra("data")!!
        text_wname.text = walletDatas.name
        cal.time = Date()
        naviView.setNavigationItemSelectedListener(this)// 네비게이션 메뉴 아이템에 클릭 속성 부여
    }

    private fun getExtraLogin() {
        access_token = intent.getStringExtra("access_token")!!
        refresh_token = intent.getStringExtra("refresh_token")!!
        user_email = intent.getStringExtra("userId")!!
        text_email.text = user_email
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

    @SuppressLint("NotifyDataSetChanged")
    private fun menuRecycler() {
        profileAdapter = ProfileAdapter(this,access_token,refresh_token,user_email)
        rv_profile.adapter = profileAdapter

        datas.apply {
            add(ProfileData(name = "name"))
            profileAdapter.datas = datas
            profileAdapter.notifyDataSetChanged()
        }
    }

    //recycler항목 추가
    @SuppressLint("NotifyDataSetChanged")
    private fun initRecycler() {
        money_profileAdapter = MoneyProfileAdapter(this)
        money_rv_profile.adapter = money_profileAdapter

        money_datas.apply {
            add(MoneyProfileData(detail = "zz", price = 10000, category = "zz"))
            money_profileAdapter.datas = money_datas
            money_profileAdapter.notifyDataSetChanged()
        }
    }

    // 네비게이션 메뉴 아이템 클릭 시 수행
    @SuppressLint("NotifyDataSetChanged")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                val dlg = Dialog(this@WalletActivity)
                dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
                dlg.setContentView(R.layout.wallet_create_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴
                dlg.show()

                val et_wname = dlg.findViewById<EditText>(R.id.et_wname)
                val btn_create = dlg.findViewById<Button>(R.id.btn_create)
                val btn_cancle = dlg.findViewById<Button>(R.id.btn_cancel)

                btn_create.setOnClickListener {
                    datas.apply {
                        add(ProfileData(name = "${et_wname?.text}"))
                        profileAdapter.datas = datas
                        profileAdapter.notifyDataSetChanged()
                    }
                    dlg.dismiss()
                }

                btn_cancle.setOnClickListener {
                    dlg.dismiss()
                }
            }
            R.id.test2 -> Toast.makeText(applicationContext, "test2", Toast.LENGTH_SHORT).show()
            R.id.test3 -> Toast.makeText(applicationContext, "test3", Toast.LENGTH_SHORT).show()
        }
        layout_drawer.closeDrawers() //네비게이션 뷰 닫기
        return false
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
}