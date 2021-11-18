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
    private val df = SimpleDateFormat("yyyy/MM")
    @SuppressLint("SimpleDateFormat")
    private val yf = SimpleDateFormat("yyyy")
    @SuppressLint("SimpleDateFormat")
    private val mf = SimpleDateFormat("MM")
    @SuppressLint("SimpleDateFormat")
    private val qf = SimpleDateFormat("dd")
    @SuppressLint("SimpleDateFormat")
    private val tf = SimpleDateFormat("yyyyMMddHHmmssSSZZ")

    private var totalPrice: Long = 0
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
        initRecycler()
        //menu recyceler항목 추가
        menuRecycler()
        //총액계산
        initTotalPrice()

        getTransaction(bearerAccessToken)

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

    private fun initTotalPrice(){
        val plus = money_datas_list.datas.filter{it.is_consuption == 0}
        val minus = money_datas_list.datas.filter { it.is_consuption == 1}
        plus.forEach { totalPrice += it.price }
        minus.forEach { totalPrice -= it.price }
        val format = DecimalFormat("#,###")
        val strPrice = format.format(totalPrice)
        text_price.text = strPrice + "원"
    }

    private fun calTotalPrice(is_consumtion: Int, price: Int){
        if(is_consumtion == 0)
            totalPrice += price
        else
            totalPrice -= price
        val format = DecimalFormat("#,###")
        val strPrice = format.format(totalPrice)
        text_price.text = strPrice + "원"
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

        SpinnerArray.sData = category.category
        SpinnerArray.sData2 = consumtion.consumtion
    }

    //token과 유저정보 가져옴
    private fun getExtraLogin() {
        access_token = intent.getStringExtra("access_token")!!
        refresh_token = intent.getStringExtra("refresh_token")!!
        user_email = intent.getStringExtra("userId")!!
        accountId = intent.getIntExtra("accountId", -1)
        text_email.text = user_email
        bearerAccessToken = "Bearer $access_token"

        money_profileAdapter = MoneyProfileAdapter(this,this,accountId,bearerAccessToken)
        money_rv_profile.adapter = money_profileAdapter
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
        profileAdapter = ProfileAdapter(this,access_token,refresh_token,user_email,accountId)
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
            val filterDatas = total_datas_list.filter{(it.date.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.date.year == (ThisTime.cal.get(Calendar.YEAR).toString()))}.toMutableList()
            money_profileAdapter.datas = filterDatas
            money_profileAdapter.notifyDataSetChanged()
        }
    }


    //수입내역표시
    @SuppressLint("NotifyDataSetChanged")
    private fun incomeInitRecycler() {
        money_datas_list.datas.apply {
            val filterDatas = (money_datas_list.datas.filter{(it.date.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.date.year == (ThisTime.cal.get(Calendar.YEAR).toString())) && (it.is_consuption == 0)}).toMutableList()
            money_profileAdapter.datas = filterDatas
            money_profileAdapter.notifyDataSetChanged()
        }
    }

    //지출내역표시
    @SuppressLint("NotifyDataSetChanged")
    private fun expenseInitRecycler() {
        money_datas_list.datas.apply {
            val filterDatas = (money_datas_list.datas.filter{(it.date.date.month == String.format("%02d",(ThisTime.cal.get(Calendar.MONTH)+1))) && (it.date.date.year == (ThisTime.cal.get(Calendar.YEAR).toString())) && (it.is_consuption == 1)}).toMutableList()
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

        var is_consumtion = 0
        var category = 0

        val c = System.currentTimeMillis()
        val a = Date(c)
        val time = tf.format(a)

        val strYear = yf.format(a)
        val strMonth = mf.format(a)
        val strDay = qf.format(a)

        val date = Date(String.format("%02d", year.value),
            String.format("%02d", month.value),
            String.format("%02d", day.value),
            time)

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

        val dateData = MoneyProfileData(
            id = 0, is_consuption = is_consumtion, price = 0, detail = "", date = DateType(
                MoneyProfileData.DATE_TYPE, date
            ),
            category = 0, account_id = accountId
        )

        val priceData = MoneyProfileData(
            id = 0, is_consuption = is_consumtion, price = et_price.text.toString().toInt(),
            detail = et_detail.text.toString(), date = DateType(
                MoneyProfileData.PRICE_TYPE, date
            ), category = category, account_id = accountId
        )

        btn_choice.setOnClickListener {
            if(et_detail.text.toString() == "" || et_price.text.toString() == ""){
                text_alarm.text = "사용내역과 금액을 입력해 주세요."
            }
            else {
                calTotalPrice(is_consumtion,et_price.text.toString().toInt())
                postTransaction(bearerAccessToken, Transaction(is_consumtion,et_price.text.toString().toInt(),et_detail.text.toString(),date,category,accountId),dateData,priceData)
                money_datas_list.datas = money_datas_list.datas.distinct().toMutableList()
                money_datas_list.datas.sortWith(compareByDescending<MoneyProfileData> { it.date.date.year }.thenByDescending { it.date.date.month }
                    .thenByDescending { it.date.date.day }.thenByDescending { it.date.type })
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
                        MoneyProfileDataList.datas.add(MoneyProfileData(id=it.id, is_consuption = it.is_consuption, price = it.price, date = DateType(MoneyProfileData.PRICE_TYPE,it.date),detail = it.detail,category = it.category,account_id = it.account_id))
                        MoneyProfileDataList.datas.add(MoneyProfileData(id=it.id, is_consuption = it.is_consuption, price = 0, date = DateType(MoneyProfileData.DATE_TYPE,it.date),detail = "",category = 0,account_id = it.account_id))
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
                    dateData.id = responseApi!!.id
                    priceData.id = responseApi.id
                    money_datas_list.datas.apply {
                        add(dateData)
                        add(priceData)
                    }
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