package com.checkmoney

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.checkmoney.account.CalTotal
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class WalletActivity : AppCompatActivity(), CalTotal,NavigationView.OnNavigationItemSelectedListener {
    private val money_datas_list = MoneyProfileDataList

    private lateinit var walletDatas: ProfileData
    private lateinit var money_profileAdapter: MoneyProfileAdapter
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var money_rv_profile: RecyclerView
    private lateinit var rv_profile: RecyclerView
    private lateinit var layout_drawer: DrawerLayout
    private lateinit var nav_header: View
    private lateinit var text_wname: TextView
    private lateinit var text_price: TextView
    private lateinit var img_profile: ImageView
    private lateinit var btn_left: ImageView
    private lateinit var btn_right: ImageView
    private lateinit var btn_total: TextView
    private lateinit var btn_expense: TextView
    private lateinit var btn_income: TextView
    private lateinit var btn_navi: ImageView
    private lateinit var btn_add: Button
    private lateinit var text_ym: TextView
    private lateinit var text_email: TextView
    private lateinit var text_name: TextView
    private lateinit var naviView: NavigationView

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var access_token: String
    private lateinit var refresh_token: String
    private lateinit var bearerAccessToken: String

    private var editUserPassword = ""
    private var editUserName = ""
    private var userName = ""
    private var userEmail = ""
    private var name_count = 0
    private var pw_count = 0

    @SuppressLint("SimpleDateFormat")
    private val df = SimpleDateFormat("yyyy/MM")
    @SuppressLint("SimpleDateFormat")
    private val yf = SimpleDateFormat("yyyy")
    @SuppressLint("SimpleDateFormat")
    private val mf = SimpleDateFormat("MM")
    @SuppressLint("SimpleDateFormat")
    private val qf = SimpleDateFormat("dd")

    private var totalPrice: Long = 0
    private var accountId = -1
    private val TAG = "WalletActivity"
    private val TAG2 = "WalletActivity_API"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        // 변수 초기화
        setVariable()
        // drawer layout 사이즈 조절
        setLayoutSize()
        // 구글 세팅
        googleBuildIn()
        // access token, refresh token, 사용자 이메일을 LoginActivity에서 받아옴
        initSetting()
        // recycler항목 추가
        initRecycler()
        // menu recyceler항목 추가
        menuRecycler()
        // 총액계산
        initTotalPrice()

        getTransaction(bearerAccessToken)

        // 전 월로 이동
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

        // 다음 월로 이동
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

        // 합계 표시
        btn_total.setOnClickListener {
            btn_total.setTypeface(null, Typeface.BOLD)
            btn_expense.typeface = Typeface.DEFAULT
            btn_income.typeface = Typeface.DEFAULT
            ListType.listype = ListType.TOTAL
            initRecycler()
        }

        // 지출 표시
        btn_expense.setOnClickListener {
            btn_total.typeface = Typeface.DEFAULT
            btn_expense.setTypeface(null, Typeface.BOLD)
            btn_income.typeface = Typeface.DEFAULT
            ListType.listype = ListType.EXPENSE
            expenseInitRecycler()
        }

        // 수입 표시
        btn_income.setOnClickListener {
            btn_total.typeface = Typeface.DEFAULT
            btn_expense.typeface = Typeface.DEFAULT
            btn_income.setTypeface(null, Typeface.BOLD)
            ListType.listype = ListType.INCOME
            incomeInitRecycler()
        }

        // 내역 추가
        btn_add.setOnClickListener {
            addDetail()
        }

        btn_navi.setOnClickListener {
            layout_drawer.openDrawer(GravityCompat.START)
        }

        btn_total.callOnClick()
    }

    private fun calTotalPrice(is_consumption: Int, price: Int){
        if(is_consumption == 0)
            totalPrice += price
        else
            totalPrice -= price
        val format = DecimalFormat("#,###")
        val strPrice = format.format(totalPrice)

        text_price.text = strPrice + "원"
        if(totalPrice < 0)
            text_price.setTextColor(
                ContextCompat.getColor(this,
                    R.color.red
                ))
        else
            text_price.setTextColor(
                ContextCompat.getColor(this,
                    R.color.logoBlue
                ))
    }

    // 수입내역표시
    @SuppressLint("NotifyDataSetChanged")
    private fun incomeInitRecycler() {
        // 필터링으로 수입만 표시
        money_datas_list.datas.apply {
            val filterDatas = (money_datas_list.datas.filter{(it.date.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.date.year == (ThisTime.cal.get(Calendar.YEAR).toString())) && (it.is_consumption == 0)}).toMutableList()
            money_profileAdapter.datas = filterDatas
            money_profileAdapter.notifyDataSetChanged()
        }
    }

    // 지출내역표시
    @SuppressLint("NotifyDataSetChanged")
    private fun expenseInitRecycler() {
        // 필터링으로 지출만 표시
        money_datas_list.datas.apply {
            val filterDatas = (money_datas_list.datas.filter{(it.date.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.date.year == (ThisTime.cal.get(Calendar.YEAR).toString())) && (it.is_consumption == 1)}).toMutableList()
            money_profileAdapter.datas = filterDatas
            money_profileAdapter.notifyDataSetChanged()
        }
    }

    //사용내역추가
    private fun addDetail() {
        // 내역추가 dlg
        val dlg = Dialog(this@WalletActivity)
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(R.layout.dialog_datepicker)     //다이얼로그에 사용할 xml 파일을 불러옴
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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


        val adapter = ArrayAdapter(this, R.layout.spinner_layout, SpinnerArray.sData)
        spinner.adapter = adapter

        val adapter2 = ArrayAdapter(this,R.layout.spinner_layout, SpinnerArray.sData2)
        spinner2.adapter = adapter2

        // datepicker의 회전여부
        year.wrapSelectorWheel = false
        month.wrapSelectorWheel = false
        day.wrapSelectorWheel = false

        // 최소값 설정
        year.minValue = 2021
        month.minValue = 1
        day.minValue = 1

        // 최대값 설정
        year.maxValue = 2025
        month.maxValue = 12
        day.maxValue = 31

        // 수입, 지출 여부
        var is_consumtion = 0
        // 카테고리 포지션
        var category = 0

        // 현재 시각
        val c = System.currentTimeMillis()
        val a = Date(c)

        val strYear = yf.format(a)
        val strMonth = mf.format(a)
        val strDay = qf.format(a)

        year.value = strYear.toInt()
        month.value = strMonth.toInt()
        day.value = strDay.toInt()

        spinner.setSelection(0)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                category = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        spinner2.setSelection(0)
        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(SpinnerArray.sData2[position] == "지출"){
                    is_consumtion = 1
                }
                else
                    is_consumtion = 0
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
                val date = Date(String.format("%02d", year.value),
                    String.format("%02d", month.value),
                    String.format("%02d", day.value),)
                calTotalPrice(is_consumtion,et_price.text.toString().toInt())
                // 날짜 데이터
                val dateData = MoneyProfileData(
                    is_consumption = is_consumtion, price = 0, detail = "", date = DateType(
                        0, MoneyProfileData.DATE_TYPE, date
                    ),
                    category = 0, account_id = accountId
                )
                // 내역 데이터
                val priceData = MoneyProfileData(
                    is_consumption = is_consumtion, price = et_price.text.toString().toInt(),
                    detail = et_detail.text.toString(), date = DateType(
                        0,MoneyProfileData.PRICE_TYPE, date
                    ), category = category, account_id = accountId
                )

                // 추가한 내역 서버로 전송
                val transaction = Transaction(is_consumption = is_consumtion,price = et_price.text.toString().toInt(),detail = et_detail.text.toString(),date="${year.value}-${month.value}-${day.value}",category = category,account_id = accountId)
                postTransaction(bearerAccessToken, transaction, dateData, priceData)
                dlg.dismiss()
                text_alarm.text = ""
            }
        }

        btn_cancle.setOnClickListener {
            dlg.dismiss()
        }
    }

    //변수 초기화 및 세팅
    private fun setVariable() {
        layout_drawer = findViewById(R.id.layout_drawer)
        naviView = findViewById(R.id.naviView)
        nav_header = naviView.getHeaderView(0)
        text_wname = findViewById(R.id.text_wname)
        text_price = findViewById(R.id.text_price)
        money_rv_profile = findViewById(R.id.rv_profile)
        rv_profile = nav_header.findViewById(R.id.rv_profile)
        img_profile = nav_header.findViewById(R.id.img_profile)
        btn_left = findViewById(R.id.btn_left)
        btn_right = findViewById(R.id.btn_right)
        btn_total = findViewById(R.id.btn_total)
        btn_expense = findViewById(R.id.btn_expense)
        btn_income = findViewById(R.id.btn_income)
        btn_navi = findViewById(R.id.btn_navi)
        btn_add = findViewById(R.id.btn_add)
        text_ym = findViewById(R.id.text_ym)
        text_email = nav_header.findViewById(R.id.text_email)
        text_name = nav_header.findViewById(R.id.text_name)
    }

    //drawer layout 사이즈 조절
    private fun setLayoutSize() {
        val display = windowManager.defaultDisplay // in case of Activity
        val size = Point()
        display.getRealSize(size) // or getSize(size)
        val width = size.x * (0.8)
        val height = size.y * (0.6)
        nav_header.layoutParams.height = height.toInt()
        naviView.layoutParams.width= width.toInt()
    }

    //token과 유저정보 가져옴, 그 외 세팅
    private fun initSetting() {
        text_ym.text = df.format(ThisTime.cal.time)
        walletDatas = intent.getParcelableExtra("data")!!
        text_wname.text = walletDatas.title
        ThisTime.cal.time = Date()
        naviView.setNavigationItemSelectedListener(this)// 네비게이션 메뉴 아이템에 클릭 속성 부여

        SpinnerArray.sData = category.category
        SpinnerArray.sData2 = consumption.consumption

        access_token = intent.getStringExtra("access_token")!!
        refresh_token = intent.getStringExtra("refresh_token")!!
        accountId = intent.getIntExtra("accountId", -1)
        bearerAccessToken = "Bearer $access_token"

        money_profileAdapter = MoneyProfileAdapter(this,this,accountId,bearerAccessToken)
        money_rv_profile.adapter = money_profileAdapter
    }

    //recycler항목 추가, 전체내역표시
    @SuppressLint("NotifyDataSetChanged")
    private fun initRecycler() {
        money_datas_list.datas.apply {
            val total_datas_list = money_datas_list.datas.distinctBy { MoneyProfileData -> MoneyProfileData.date}
            val filterDatas = total_datas_list.filter{(it.date.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.date.year == (ThisTime.cal.get(Calendar.YEAR).toString()))}.toMutableList()
            money_profileAdapter.datas = filterDatas
            money_profileAdapter.notifyDataSetChanged()
        }
    }

    //통장목록표시
    @SuppressLint("NotifyDataSetChanged")
    private fun menuRecycler() {
        profileAdapter = ProfileAdapter(this,access_token,refresh_token,accountId,layout_drawer)
        rv_profile.adapter = profileAdapter

        ProfileDataList.datas.apply {
            Log.d(TAG,"Profile Data list" + ProfileDataList.datas.toString())
            profileAdapter.datas = ProfileDataList.datas
            profileAdapter.notifyDataSetChanged()
        }
    }

    private fun initTotalPrice(){
        val plus = money_datas_list.datas.filter{it.is_consumption == 0}
        val minus = money_datas_list.datas.filter { it.is_consumption == 1}
        plus.forEach { totalPrice += it.price }
        minus.forEach { totalPrice -= it.price }
        val format = DecimalFormat("#,###")
        val strPrice = format.format(totalPrice)
        text_price.text = strPrice + "원"
    }

    // 네비게이션 메뉴 아이템 클릭 시 수행
    @SuppressLint("NotifyDataSetChanged")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                val dlg = Dialog(this@WalletActivity)
                dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
                dlg.setContentView(R.layout.dialog_wallet_create)     //다이얼로그에 사용할 xml 파일을 불러옴
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
            R.id.home -> {
                val mainIntent = Intent(this, MainActivity::class.java)
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(mainIntent)
            }
            R.id.edit -> {

            }

            R.id.logout -> {
                ProfileDataList.datas.clear()
                AppPref.prefs.clearUser(this)
                googleSignOut()
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
                finish()
            }
        }
        return false
    }

    override fun calTotal(deleteConsum: Int, deletePrice: Int, addConsum: Int, addPrice: Int) {
        if(deleteConsum == 0)
            totalPrice -= deletePrice
        else
            totalPrice += deletePrice
        if(addConsum == 0)
            totalPrice += addPrice
        else
            totalPrice -= addPrice
        val format = DecimalFormat("#,###")
        val strPrice = format.format(totalPrice)
        text_price.text = strPrice + "원"
        if(totalPrice < 0)
            text_price.setTextColor(
                ContextCompat.getColor(this,
                R.color.red
            ))
        else
            text_price.setTextColor(
                ContextCompat.getColor(this,
                    R.color.logoBlue
                ))
    }

    override fun onBackPressed() {
        if (layout_drawer.isDrawerOpen(GravityCompat.START)) {
            layout_drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed()
        }
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
        RetrofitBuild.api.postAccount(accessToken, account).enqueue(object : Callback<ResultId> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResultId>, response: Response<ResultId>) {
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
            override fun onFailure(call: Call<ResultId>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }

    private fun getTransaction(accessToken: String){
        RetrofitBuild.api.getTransaction(accessToken, accountId).enqueue(object : Callback<ResultTransactions> {
            override fun onResponse(call: Call<ResultTransactions>, response: Response<ResultTransactions>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                    responseApi!!.rows.forEach {
                        var arr = it.date.split("-")
                        MoneyProfileDataList.datas.add(MoneyProfileData(is_consumption = it.is_consumption, price = it.price, date = DateType(it.id, MoneyProfileData.PRICE_TYPE,Date(year = arr[0], month = arr[1], day = arr[2])),detail = it.detail,category = it.category,account_id = it.account_id))
                        MoneyProfileDataList.datas.add(MoneyProfileData(is_consumption = it.is_consumption, price = 0, date = DateType(-1, MoneyProfileData.DATE_TYPE,Date(year = arr[0], month = arr[1], day = arr[2])),detail = "",category = 0,account_id = it.account_id))
                        calTotalPrice(it.is_consumption, it.price)
                    }
                    money_datas_list.datas = money_datas_list.datas.distinct().toMutableList()
                    money_datas_list.datas.sortWith(compareByDescending<MoneyProfileData> { it.date.date.year }.thenByDescending { it.date.date.month }
                        .thenByDescending { it.date.date.day }.thenByDescending { it.date.type })
                    btn_total.callOnClick()
                } else { // code == 400
                    Log.d(TAG2, "연결실패")
                }
            }
            override fun onFailure(call: Call<ResultTransactions>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }

    private fun postTransaction(accessToken: String, transaction: Transaction,dateData: MoneyProfileData, priceData: MoneyProfileData) {
        RetrofitBuild.api.postTransaction(accessToken, transaction).enqueue(object : Callback<ResultId> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResultId>, response: Response<ResultId>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    dateData.date.id = -1
                    priceData.date.id = responseApi!!.id
                    money_datas_list.datas.apply {
                        add(dateData)
                        add(priceData)
                    }
                    money_datas_list.datas = money_datas_list.datas.distinct().toMutableList()
                    money_datas_list.datas.sortWith(compareByDescending<MoneyProfileData> { it.date.date.year }.thenByDescending { it.date.date.month }
                        .thenByDescending { it.date.date.day }.thenByDescending { it.date.type })
                    Log.d("!!!!!!!!!!!!!!!!!!!!!",money_datas_list.datas.toString())
                    btn_total.callOnClick()
                    Log.d(TAG2,responseApi.toString())
                } else { // code == 400
                    Log.d(TAG2, "연결실패")
                }
            }
            override fun onFailure(call: Call<ResultId>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }
}