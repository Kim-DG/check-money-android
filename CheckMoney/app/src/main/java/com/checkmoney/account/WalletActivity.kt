package com.checkmoney

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Point
import android.graphics.Typeface
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class WalletActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val money_datas_list = MoneyProfileDataList

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
    private lateinit var btn_add: Button
    private lateinit var text_ym: TextView
    private lateinit var text_email: TextView
    private lateinit var naviView: NavigationView

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var access_token: String
    private lateinit var refresh_token: String
    private lateinit var user_email: String
    private lateinit var bearerAccessToken: String

    @SuppressLint("SimpleDateFormat")
    private var df = SimpleDateFormat("yyyy/MM")
    @SuppressLint("SimpleDateFormat")
    private var tf = SimpleDateFormat("yyyyMMddHHmmssSSZZ")

    private var accountId = -1
    private val TAG = "WalletActivity"
    private val TAG2 = "WalletActivity_API"

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

        //money_datas_list.datas.add(MoneyProfileData(date = Date(MoneyProfileData.DATE_TYPE,"2021","11","11",""), detail = "", positive = "positive", price = 0, category = ""))
       // money_datas_list.datas.add(MoneyProfileData(date = Date(MoneyProfileData.PRICE_TYPE,"2021","11","11", Calendar.getInstance().toString()), detail = "xx", positive = "positive", price = 100050, category = "xx"))
        //money_datas_list.datas = money_datas_list.datas.distinct().toMutableList()
       // money_datas_list.datas.sortWith(compareByDescending<MoneyProfileData>{it.date.year}.thenByDescending { it.date.month }.thenByDescending { it.date.day }.thenByDescending { it.date.type })

        initRecycler()
        //menu recyceler항목 추가
        menuRecycler()

        btn_total.setOnClickListener {
            btn_total.setTypeface(null, Typeface.BOLD)
            btn_expense.typeface = Typeface.DEFAULT
            btn_income.typeface = Typeface.DEFAULT
            ListType.listype = ListType.TOTAL
            initRecycler()
        }

        btn_left.setOnClickListener {
            if(ThisTime.cal.get(Calendar.MONTH) == 0) {
                ThisTime.cal.add(Calendar.MONTH, 11)
                ThisTime.cal.add(Calendar.YEAR, -1)
            }
            else{
                ThisTime.cal.add(Calendar.MONTH, -1)
            }
            text_ym.text = df.format(ThisTime.cal.time)
            Log.d("@@@@@@@@@@@@@@@@@@@@@", ThisTime.cal.get(Calendar.MONTH).toString())
            btn_total.callOnClick()
        }

        btn_right.setOnClickListener {
            if(ThisTime.cal.get(Calendar.MONTH) == 11) {
                ThisTime.cal.add(Calendar.MONTH, -11)
                ThisTime.cal.add(Calendar.YEAR, 1)
            }
            else{
                ThisTime.cal.add(Calendar.MONTH, 1)
            }
            text_ym.text = df.format(ThisTime.cal.time)
            btn_total.callOnClick()
        }

        btn_expense.setOnClickListener {
            btn_total.typeface = Typeface.DEFAULT
            btn_expense.setTypeface(null, Typeface.BOLD)
            btn_income.typeface = Typeface.DEFAULT
            ListType.listype = ListType.EXPENSE
            expenseInitRecycler()
        }

        btn_income.setOnClickListener {
            btn_total.typeface = Typeface.DEFAULT
            btn_expense.typeface = Typeface.DEFAULT
            btn_income.setTypeface(null, Typeface.BOLD)
            ListType.listype = ListType.INCOME
            incomeInitRecycler()
        }

        btn_add.setOnClickListener {
            addDetail()
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

        btn_total.callOnClick()
    }

    //변수 초기화 및 세팅
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
        btn_add = findViewById(R.id.btn_add)
        text_ym = findViewById(R.id.text_ym)
        text_email = nav_header.findViewById(R.id.text_email)

        text_ym.text = df.format(ThisTime.cal.time)
        walletDatas = intent.getParcelableExtra("data")!!
        text_wname.text = walletDatas.title
        ThisTime.cal.time = Date()
        naviView.setNavigationItemSelectedListener(this)// 네비게이션 메뉴 아이템에 클릭 속성 부여

        SpinnerArray.sData = resources.getStringArray(R.array.category)
        SpinnerArray.sData2 = resources.getStringArray(R.array.price)

        money_profileAdapter = MoneyProfileAdapter(this)
        money_rv_profile.adapter = money_profileAdapter
    }


    //token과 유저정보 가져옴
    private fun getExtraLogin() {
        access_token = intent.getStringExtra("access_token")!!
        refresh_token = intent.getStringExtra("refresh_token")!!
        user_email = intent.getStringExtra("userId")!!
        accountId = intent.getIntExtra("accountId", -1)
        text_email.text = user_email
        bearerAccessToken = "Bearer $access_token"
        Log.d("!!!!!!!!!!!!!!!!!!!",accountId.toString())
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

    //통장목록표시
    @SuppressLint("NotifyDataSetChanged")
    private fun menuRecycler() {
        profileAdapter = ProfileAdapter(this,access_token,refresh_token,user_email)
        rv_profile.adapter = profileAdapter

        ProfileDataList.datas.apply {
            Log.d(TAG,"Profile Data list" + ProfileDataList.datas.toString())
            profileAdapter.datas = ProfileDataList.datas
            profileAdapter.notifyDataSetChanged()
        }
    }

    //recycler항목 추가, 전체내역표시
    @SuppressLint("NotifyDataSetChanged")
    private fun initRecycler() {
        money_datas_list.datas.apply {
            val total_datas_list = money_datas_list.datas.distinctBy { MoneyProfileData -> MoneyProfileData.date}
            val filterDatas = total_datas_list.filter{(it.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.year == (ThisTime.cal.get(Calendar.YEAR).toString()))}.toMutableList()
            money_profileAdapter.datas = filterDatas
            money_profileAdapter.notifyDataSetChanged()
        }
    }

    //수입내역표시
    @SuppressLint("NotifyDataSetChanged")
    private fun incomeInitRecycler() {
        money_datas_list.datas.apply {
            val filterDatas = (money_datas_list.datas.filter{(it.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.year == (ThisTime.cal.get(Calendar.YEAR).toString())) && (it.positive == "positive")}).toMutableList()
            money_profileAdapter.datas = filterDatas
            money_profileAdapter.notifyDataSetChanged()
        }
    }

    //지출내역표시
    @SuppressLint("NotifyDataSetChanged")
    private fun expenseInitRecycler() {
        money_datas_list.datas.apply {
            val filterDatas = (money_datas_list.datas.filter{(it.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.year == (ThisTime.cal.get(Calendar.YEAR).toString())) && (it.positive == "negative")}).toMutableList()
            money_profileAdapter.datas = filterDatas
            money_profileAdapter.notifyDataSetChanged()
        }
    }

    //사용내역추가
    private fun addDetail() {
        val dlg = Dialog(this@WalletActivity)
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(R.layout.dialog_datepicker)     //다이얼로그에 사용할 xml 파일을 불러옴
        dlg.show()

        val et_detail = dlg.findViewById<EditText>(R.id.et_detail)
        val et_price = dlg.findViewById<EditText>(R.id.et_price)
        val btn_choice = dlg.findViewById<Button>(R.id.btn_choice)
        val btn_cancle = dlg.findViewById<Button>(R.id.btn_cancel)
        val spinner = dlg.findViewById<Spinner>(R.id.spinner)
        val spinner2 = dlg.findViewById<Spinner>(R.id.spinner2)
        val text_alarm = dlg.findViewById<TextView>(R.id.text_alarm)

        val year : NumberPicker = dlg.findViewById(R.id.yearpicker_datepicker)
        val month : NumberPicker = dlg.findViewById(R.id.monthpicker_datepicker)
        val day : NumberPicker = dlg.findViewById(R.id.daypicker_datepicker)


        val adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, SpinnerArray.sData)
        spinner.adapter = adapter

        val adapter2 = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1, SpinnerArray.sData2)
        spinner2.adapter = adapter2

        year.wrapSelectorWheel = false
        month.wrapSelectorWheel = false
        day.wrapSelectorWheel = false

        //  최소값 설정
        year.minValue = 2021
        month.minValue = 1
        day.minValue = 1

        //  최대값 설정
        year.maxValue = 2025
        month.maxValue = 12
        day.maxValue = 31

        var positive = ""
        var category = ""

        val c = System.currentTimeMillis()
        val a = Date(c)
        val time = tf.format(a)

        spinner.setSelection(1)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                category = SpinnerArray.sData[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        spinner2.setSelection(1)
        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(SpinnerArray.sData2[position] == "지출"){
                    positive = "negative"
                }
                else
                    positive = "positive"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        btn_choice.setOnClickListener {
            if(et_detail.text.toString() == "" || et_price.text.toString() == ""){
                text_alarm.text = "사용내역과 금액을 입력해 주세요."
            }
            else {
                money_datas_list.datas.apply {
                    add(
                        MoneyProfileData(
                            date = Date(
                                MoneyProfileData.DATE_TYPE,
                                String.format("%02d", year.value),
                                String.format("%02d", month.value),
                                String.format("%02d", day.value),
                                ""
                            ), detail = "", positive = positive,
                            price = 0, category = ""
                        )
                    )
                    add(
                        MoneyProfileData(
                            date = Date(
                                MoneyProfileData.PRICE_TYPE,
                                String.format("%02d", year.value),
                                String.format("%02d", month.value),
                                String.format("%02d", day.value),
                                time
                            ), detail = et_detail.text.toString(), positive = positive,
                            price = et_price.text.toString().toLong(), category = category
                        )
                    )
                }
                Log.d("!!!!!!!!!!!!!!!424124124",MoneyProfileDataList.datas.toString())
                money_datas_list.datas = money_datas_list.datas.distinct().toMutableList()
                money_datas_list.datas.sortWith(compareByDescending<MoneyProfileData> { it.date.year }.thenByDescending { it.date.month }
                    .thenByDescending { it.date.day }.thenByDescending { it.date.type })
                btn_total.callOnClick()
                dlg.dismiss()
                text_alarm.text = ""
            }
        }

        btn_cancle.setOnClickListener {
            dlg.dismiss()
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
                    val account = Account(title = et_wname?.text.toString(), description = "aa")
                    postAccount(bearerAccessToken, account)
                    ProfileDataList.datas.apply {
                        add(ProfileData(title = "${et_wname?.text}",id = -1))
                        profileAdapter.datas = ProfileDataList.datas
                        profileAdapter.notifyDataSetChanged()
                        Log.d("!!!!!!!!!!!!!!!!!!!!",ProfileDataList.datas.toString())
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

    //-----------------------------------------------------------------------
    //                            Rest Api function
    //-----------------------------------------------------------------------

    private fun postAccount(accessToken: String, account: Account) {
        RetrofitBuild.api.postAccount(accessToken, account).enqueue(object : Callback<ResultAccount> {
            override fun onResponse(call: Call<ResultAccount>, response: Response<ResultAccount>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
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

    private fun putAccount(accountId: Int, account: Account) {
        RetrofitBuild.api.putAccount(access_token, accountId, account).enqueue(object :
            Callback<Result> {
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                } else { // code == 400
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

    private fun deleteAccount(accountId: Int) {
        RetrofitBuild.api.deleteAccount(access_token, accountId).enqueue(object : Callback<Result> {
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                } else { // code == 400
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