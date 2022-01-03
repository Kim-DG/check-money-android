package com.checkmoney

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var mBackWait:Long = 0
    private val gson = Gson()
    private val type = object : TypeToken<ErrorResult>() {}.type

    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var layoutDrawer: DrawerLayout
    private lateinit var navHeader: View
    private lateinit var rv_profile: RecyclerView
    private lateinit var btn_navi: ImageView
    private lateinit var img_profile: CircleImageView
    private lateinit var naviView: NavigationView
    private lateinit var text_email: TextView
    private lateinit var text_name: TextView

    private lateinit var edit_user_dlg: Dialog
    private lateinit var et_name: EditText
    private lateinit var et_oldPw: EditText
    private lateinit var et_pw: EditText
    private lateinit var et_pwConfirm: EditText
    private lateinit var text_pwRegular: TextView
    private lateinit var text_pwConfirmCheck: TextView
    private lateinit var text_EditCheck: TextView
    private lateinit var text_userEmail: TextView
    private lateinit var img_profile_dlg: CircleImageView
    private lateinit var btn_edit: Button
    private lateinit var btn_cancle: Button
    private lateinit var btn_getImage: TextView
    private lateinit var body : MultipartBody.Part
    private lateinit var viewpager: ViewPager2
    private lateinit var tabs: TabLayout

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var access_token: String
    private lateinit var refresh_token: String
    private lateinit var bearerAccessToken: String
    private lateinit var pieChart: PieChart
    private lateinit var choiceImage: Bitmap
    private lateinit var allTransaction: ArrayList<TransactionModel>

    private var editOldPassword = ""
    private var editUserPassword = ""
    private var editUserName = ""
    private var userName = ""
    private var userEmail = ""
    private var userProfile: String? = null
    private var name_count = 0
    private var pw_count = 1
    private var profile_count = 0

    private var refreshToken = RefreshToken(refresh_token = "")
    private val TAG = "MainActivity"
    private val TAG2 = "MainActivity_API"
    private var accountId = -1
    private val REQUEST_OPEN_GALLERY: Int = 1
    private val REQ_PERMISSION_GALLERY = 1001
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 변수 초기화
        setVariable()
        // drawer layout 사이즈 조절
        setLayoutSize()
        // 구글 세팅
        googleBuildIn()
        // access token, refresh token, 사용자 이메일을 LoginActivity에서 받아옴
        initSetting()
        // 내 정보 받아오기
        getMyInfo(bearerAccessToken)
        // 계좌 받아오기
        getAccount(bearerAccessToken)
        // 내정보수정 dlg setting
        setEditUserInfoDlg()
        //viewpager2, tablayout 표시 및 연동

        btn_navi.setOnClickListener {
            layoutDrawer.openDrawer(GravityCompat.START)
            profileAdapter.datas = ProfileDataList.datas
            profileAdapter.notifyDataSetChanged()
        }
    }

    override fun onStart() {
        super.onStart()
        // 모든 내역 받아와서 차트 띄우기
        getAllTransAction(bearerAccessToken)
    }

    // recycler항목 추가
    @SuppressLint("NotifyDataSetChanged")
    private fun initRecycler(accountList: ArrayList<AccountModel>) {
        //어답터 생성
        profileAdapter = ProfileAdapter(this,access_token,refresh_token,accountId,layoutDrawer)
        rv_profile.adapter = profileAdapter

        ProfileDataList.datas.clear()
        // 받아온 계좌 추가
        ProfileDataList.datas.apply {
            accountList.forEach {
                add(ProfileData(title = it.title, description = it.description, id = it.id))
            }
            Log.d(TAG,"Profile Data list" + ProfileDataList.datas.toString())
            profileAdapter.datas = ProfileDataList.datas
            profileAdapter.notifyDataSetChanged()
        }
    }

    private fun renderViewPager() {
        viewpager.adapter = object : FragmentStateAdapter(this) {

            override fun createFragment(position: Int): Fragment {
                val bundle = Bundle()
                bundle.putParcelableArrayList("transaction", allTransaction)
                ResourceStore.pagerFragments[position].arguments = bundle
                return ResourceStore.pagerFragments[position]
            }

            override fun getItemCount(): Int {
                return ResourceStore.tabList.size
            }
        }
    }

    private fun renderTabLayer() {
        TabLayoutMediator(tabs, viewpager) { tab, position ->
            tab.text = ResourceStore.tabList[position]
        }.attach()
    }

    // 변수 초기화
    @SuppressLint("CutPasteId")
    private fun setVariable() {
        layoutDrawer = findViewById(R.id.layout_drawer)
        btn_navi = findViewById(R.id.btn_navi)
        naviView = findViewById(R.id.naviView)
        navHeader = naviView.getHeaderView(0)
        rv_profile = navHeader.findViewById(R.id.rv_profile)
        img_profile = navHeader.findViewById(R.id.img_profile)
        text_email = navHeader.findViewById(R.id.text_email)
        text_name = navHeader.findViewById(R.id.text_name)
        viewpager = findViewById(R.id.viewpager)
        tabs = findViewById(R.id.tabs)
    }

    // drawer layout 사이즈 조절
    private fun setLayoutSize() {
        val display = windowManager.defaultDisplay // in case of Activity
        val size = Point()
        display.getRealSize(size) // or getSize(size)
        // drawer 가로넓이
        val width = size.x * (0.8)
        // drawer 헤드길이
        val height = size.y * (0.67)
        // 길이 적용
        navHeader.layoutParams.height = height.toInt()
        naviView.layoutParams.width= width.toInt()
    }

    // access token, refresh token, 사용자 이메일을 LoginActivity에서 받아옴
    private fun initSetting() {
        // 네비게이션 메뉴 아이템에 클릭 속성 부여
        naviView.setNavigationItemSelectedListener(this)
        // 로그인페이지에서 데이터 받아옴
        val intent = getIntent()
        access_token = intent.getStringExtra("access_token")!!
        refresh_token = intent.getStringExtra("refresh_token")!!

        // refresfh토큰 초기화
        refreshToken.refresh_token = refresh_token
        // access토큰 초기화
        bearerAccessToken = "Bearer $access_token"
    }

    // 차트 생성
    private fun createPieChart() {
        // 퍼센트 사용
        pieChart.setUsePercentValues(true)
        // 설명
        pieChart.description.text = ""
        // 터치가능
        pieChart.setTouchEnabled(true)
        // 회전가능
        pieChart.isRotationEnabled = true
        // 차트안에 항목이름
        pieChart.setDrawEntryLabels(true)
        // 하단항목이름
        pieChart.legend.isEnabled = false
        //pieChart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        //pieChart.legend.isWordWrapEnabled = true
        // 차트에 데이터 추가
        val priceCategory = Array(8) { 0.0F }
        val colors: ArrayList<Int> = ArrayList()
        allTransaction.forEach {
            if(it.is_consumption == 1 && it.category == 0){
                priceCategory[0] = priceCategory[0] + it.price
            }
            else if(it.is_consumption == 1 && it.category == 1){
                priceCategory[1] = priceCategory[1] + it.price
            }
            else if(it.is_consumption == 1 && it.category == 2){
                priceCategory[2] = priceCategory[2] + it.price
            }
            else if(it.is_consumption == 1 && it.category == 3){
                priceCategory[3] = priceCategory[3] + it.price
            }
            else if(it.is_consumption == 1 && it.category == 4){
                priceCategory[4] = priceCategory[4] + it.price
            }
            else if(it.is_consumption == 1 && it.category == 5){
                priceCategory[5] = priceCategory[5] + it.price
            }
            else if(it.is_consumption == 1 && it.category == 6){
                priceCategory[6] = priceCategory[6] + it.price
            }
            else if(it.is_consumption == 1 && it.category == 7){
                priceCategory[7] = priceCategory[7] + it.price
            }
        }
        val totalPrice = priceCategory.sum()
        val dataEntries = ArrayList<PieEntry>()
        if(priceCategory[0] != 0.0F){
            val category0 = priceCategory[0] / totalPrice * 100
            dataEntries.add(PieEntry(category0, category.category[0]))
            colors.add(Color.parseColor("#0096FF"))
        }
        if(priceCategory[1] != 0.0F){
            val category0 = priceCategory[1] / totalPrice * 100
            dataEntries.add(PieEntry(category0, category.category[1]))
            colors.add(Color.parseColor("#0064FF"))
        }
        if(priceCategory[2] != 0.0F){
            val category0 = priceCategory[2] / totalPrice * 100
            dataEntries.add(PieEntry(category0, category.category[2]))
            colors.add(Color.parseColor("#4C39E1"))
        }
        if(priceCategory[3] != 0.0F){
            val category0 = priceCategory[3] / totalPrice * 100
            dataEntries.add(PieEntry(category0, category.category[3]))
            colors.add(Color.parseColor("#FFF176"))
        }
        if(priceCategory[4] != 0.0F){
            val category0 = priceCategory[4] / totalPrice * 100
            dataEntries.add(PieEntry(category0, category.category[4]))
            colors.add(Color.parseColor("#FF8A65"))
        }
        if(priceCategory[5] != 0.0F){
            val category0 = priceCategory[5] / totalPrice * 100
            dataEntries.add(PieEntry(category0, category.category[5]))
            colors.add(Color.parseColor("#CAF0F8"))
        }
        if(priceCategory[6] != 0.0F){
            val category0 = priceCategory[6] / totalPrice * 100
            dataEntries.add(PieEntry(category0, category.category[6]))
            colors.add(Color.parseColor("#90E0EF"))
        }
        if(priceCategory[7] != 0.0F){
            val category0 = priceCategory[7] / totalPrice * 100
            dataEntries.add(PieEntry(category0, category.category[7]))
            colors.add(Color.parseColor("#90C8EF"))
        }

        // dataset
        val dataSet = PieDataSet(dataEntries, "")
        // 개별 data
        val data = PieData(dataSet)

        // In Percentage
        data.setValueFormatter(PercentFormatter())
        // 항목간 여백 길이
        dataSet.sliceSpace = 1f
        // 차트 색깔 적용
        dataSet.colors = colors
        // 차트에 data 적용
        pieChart.data = data
        // data 글자 크기
        data.setValueTextSize(15f)
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        // 차트 생성시 애니메이션
        pieChart.animateY(1400, Easing.EaseInOutQuad)

        // 구멍의 지름
        pieChart.holeRadius = 40f
        // 흰색 원 지름
        pieChart.transparentCircleRadius = 45f
        // 차트안 구멍 생성
        pieChart.isDrawHoleEnabled = true
        // 구멍 색
        pieChart.setHoleColor(Color.WHITE)

        pieChart.invalidate()
    }

    // 네비게이션 메뉴 아이템 클릭 시 수행
    @SuppressLint("NotifyDataSetChanged")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 계좌추가버튼 클릭 - 계좌생성 dlg
           R.id.add -> {
                val dlg = Dialog(this@MainActivity)
                dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
                dlg.setContentView(R.layout.dialog_wallet_create)     //다이얼로그에 사용할 xml 파일을 불러옴
                dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dlg.show()

                val et_wname = dlg.findViewById<EditText>(R.id.et_wname)
                val et_description = dlg.findViewById<EditText>(R.id.et_description)
                val btn_create = dlg.findViewById<Button>(R.id.btn_create)
                val btn_cancle = dlg.findViewById<Button>(R.id.btn_cancel)

               // 계좌 생성
                btn_create.setOnClickListener {
                    val account = Account(title = et_wname?.text.toString(), description = et_description?.text.toString())
                    postAccount(bearerAccessToken, account)
                    dlg.dismiss()
                }
                btn_cancle.setOnClickListener {
                    dlg.dismiss()
                }
            }
            // 홈버튼 클릭 - 메인화면
            R.id.home -> {
                layoutDrawer.closeDrawer(GravityCompat.START)
            }
            // 내정보수정버튼 클릭 - 정보수정 dlg
            R.id.edit -> {
                editUserInfo()
            }
            // 로그아웃버튼 클릭 - 로그아웃
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

    // 뒤로가기 두번클릭 시 앱 종료
    override fun onBackPressed() {
        // drawer가 켜져있으면 닫기
        if (layoutDrawer.isDrawerOpen(GravityCompat.START)) {
            layoutDrawer.closeDrawer(GravityCompat.START)
        }
        // 아닐 시 두번누르면 앱 종료
        else {
            if (System.currentTimeMillis() - mBackWait >= 2000) {
                mBackWait = System.currentTimeMillis()
                Toast.makeText(this, " 한 번 더 누르면 종료됩니다.", Toast.LENGTH_LONG).show()
            } else {
                finishAffinity()
                System.runFinalization()
                System.exit(0)
            }
        }
    }

    //-----------------------------------------------------------------------
    //                             Edit My Info
    //-----------------------------------------------------------------------

    // 내정보수정 세팅
    private fun setEditUserInfoDlg(){
        edit_user_dlg = Dialog(this@MainActivity)
        edit_user_dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        edit_user_dlg.setContentView(R.layout.dialog_userinfo_edit)     //다이얼로그에 사용할 xml 파일을 불러옴
        edit_user_dlg.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)
        edit_user_dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        et_name = edit_user_dlg.findViewById(R.id.et_name)
        et_oldPw = edit_user_dlg.findViewById(R.id.et_oldPassword)
        et_pw = edit_user_dlg.findViewById(R.id.et_password)
        et_pwConfirm = edit_user_dlg.findViewById(R.id.et_password_check)
        text_pwRegular = edit_user_dlg.findViewById(R.id.text_pw_regular)
        text_pwConfirmCheck = edit_user_dlg.findViewById(R.id.text_pw_confirm_result)
        text_EditCheck = edit_user_dlg.findViewById(R.id.text_edit_check)

        text_userEmail = edit_user_dlg.findViewById(R.id.text_userEmail)
        img_profile_dlg = edit_user_dlg.findViewById(R.id.img_profile)
        btn_edit = edit_user_dlg.findViewById(R.id.btn_edit)
        btn_cancle = edit_user_dlg.findViewById(R.id.btn_cancel)
        btn_getImage = edit_user_dlg.findViewById(R.id.btn_getImage)
    }

    // 내정보수정 dlg
    private fun editUserInfo() {
        edit_user_dlg.show()
        profile_count = 0
        getMyInfo(bearerAccessToken)
        if(userProfile == null) {
            img_profile_dlg.setImageResource(R.drawable.profile)
        }
        else{
            val url =
                "https://checkmoneyproject.azurewebsites.net/api$userProfile"
            Glide.with(this@MainActivity).load(url).into(img_profile_dlg)
        }

        pwCheck()
        nameCheck()

        btn_edit.setOnClickListener {
            userEdit()
        }

        btn_cancle.setOnClickListener {
            text_EditCheck.text = ""
            edit_user_dlg.dismiss()
        }
        btn_getImage.setOnClickListener {
            val dlg = Dialog(this@MainActivity)
            dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
            dlg.setContentView(R.layout.dialog_choice_profile)     //다이얼로그에 사용할 xml 파일을 불러옴
            dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dlg.show()

            val btn_basic: Button = dlg.findViewById(R.id.btn_basic)
            val btn_gallery: Button = dlg.findViewById(R.id.btn_gallery)
            val btn_cancle: Button = dlg.findViewById(R.id.btn_cancel)

            btn_basic.setOnClickListener {
                img_profile.setImageResource(R.drawable.profile)
                img_profile_dlg.setImageResource(R.drawable.profile)
                userProfile = null
                dlg.dismiss()
            }

            btn_gallery.setOnClickListener {
                try {
                    if(galleryPermissionGranted(edit_user_dlg)) {
                        openGallery()
                    }
                } catch (e: ActivityNotFoundException) {
                    Log.e(TAG, e.message.toString())
                }
                dlg.dismiss()
            }

            btn_cancle.setOnClickListener {
                dlg.dismiss()
            }
        }
    }

    // 갤러리 오픈
    private fun openGallery(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_OPEN_GALLERY)
    }

    // 비밀번호 조건 체크
    private fun pwCheck() {
        var regular_count = 0
        et_oldPw.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                editOldPassword = et_oldPw.text.toString()
                if(editOldPassword == "" && editUserPassword == ""){
                    pw_count = 1
                }else if(editOldPassword != "" && editUserPassword == ""){
                    pw_count = 0
                }
            }
        })
        et_pw.addTextChangedListener(object: TextWatcher {
            var pw_first = et_pw.text.toString()
            var pw_check = et_pwConfirm.text.toString()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pw_first = et_pw.text.toString()
                pw_check = et_pwConfirm.text.toString()
                editUserPassword = pw_first
                if(pw_first != ""){
                    val regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$".toRegex()
                    if (!regex.containsMatchIn(pw_first)){
                        text_pwRegular.text = "영문+숫자+특수문자를 포함하여 8자리 이상을 입력해 주세요."
                        regular_count = 0
                    }
                    else{
                        text_pwRegular.text = ""
                        regular_count = 1
                    }
                }
                else{
                    text_pwRegular.text = ""
                }
                if(pw_check != "") {
                    if (pw_first == pw_check) {
                        text_pwConfirmCheck.setTextColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.logoBlue
                            )
                        )
                        text_pwConfirmCheck.text = "비밀번호가 일치합니다."
                        pw_count = if(regular_count == 1) {
                            1
                        }else 0
                    } else {
                        text_pwConfirmCheck.setTextColor(ContextCompat.getColor(this@MainActivity,
                            R.color.red
                        ))
                        text_pwConfirmCheck.text = "비밀번호가 일치하지 않습니다."
                        pw_count = 0
                    }
                }
                else {
                    text_pwConfirmCheck.text = ""
                    pw_count = if(regular_count == 1) {
                        1
                    }else 0
                }
            }
            override fun afterTextChanged(s: Editable?) {
                if(editOldPassword == "" && editUserPassword == ""){
                    pw_count = 1
                }
            }
        })

        et_pwConfirm.addTextChangedListener(object: TextWatcher {
            var pw_first = et_pw.text.toString()
            var pw_check = et_pwConfirm.text.toString()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pw_first = et_pw.text.toString()
                pw_check = et_pwConfirm.text.toString()
                if(pw_check != "") {
                    if (pw_first == pw_check) {
                        text_pwConfirmCheck.setTextColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.logoBlue
                            )
                        )
                        text_pwConfirmCheck.text = "비밀번호가 일치합니다."
                        pw_count = if(regular_count == 1) {
                            1
                        }else 0
                    } else {
                        text_pwConfirmCheck.setTextColor(ContextCompat.getColor(this@MainActivity,
                            R.color.red
                        ))
                        text_pwConfirmCheck.text = "비밀번호가 일치하지 않습니다."
                        pw_count = 0
                    }
                }
                else {
                    text_pwConfirmCheck.text = ""
                    pw_count = 0
                }

            }
            override fun afterTextChanged(s: Editable?) {
                if(editOldPassword == "" && editUserPassword == ""){
                    pw_count = 1
                }
            }
        })
    }

    // 이름 적었나 체크
    private fun nameCheck(){
        et_name.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                if(et_name.text.toString() != ""){
                    name_count = 1
                    editUserName = et_name.text.toString()
                }
                else{
                    name_count = 0
                }
            }
        })
    }

    // 수정 완료
    private fun userEdit() {
        if(pw_count == 1 && name_count == 1){
            if(profile_count == 1) {
                postImage(bearerAccessToken, body)
                img_profile.setImageBitmap(choiceImage)
            }
            else {
                if (editUserPassword == "") {
                    val myInfo = EditMyInfo(null, editUserName, null, null)
                    text_name.text = editUserName
                    putMyInfo(bearerAccessToken, myInfo)
                } else {
                    val myInfo =
                        EditMyInfo(null, editUserName, editOldPassword, editUserPassword)
                    text_name.text = editUserName
                    putMyInfo(bearerAccessToken, myInfo)
                }
            }
        }
        else{
            text_EditCheck.text="다시 한번 확인하여 주십시오."
        }
    }

    // 갤러리 오픈
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_OPEN_GALLERY) {
            if(resultCode == RESULT_OK) {
                val currentImageUri = data?.data

                try{
                    currentImageUri?.let {
                        choiceImage = MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            currentImageUri
                        )
                    }
                    val absolutePath = getFullPathFromUri(this,currentImageUri)
                    val file = File(absolutePath!!)
                    val requestFile = RequestBody.create(MediaType.parse("MultipartBody.Part"), file)
                    body = MultipartBody.Part.createFormData("img", file.path,requestFile)
                    profile_count = 1
                    img_profile_dlg.setImageBitmap(choiceImage)
                }catch(e: Exception) {
                    //e.printStackTrace()
                }
            }
        }
    }

    // 절대경로 구하기
    private fun getFullPathFromUri(ctx: Context, fileUri: Uri?): String? {
        var fullPath: String? = null
        val column = "_data"
        var cursor = ctx.contentResolver.query(fileUri!!, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            var document_id = cursor.getString(0)
            if (document_id == null) {
                for (i in 0 until cursor.columnCount) {
                    if (column.equals(cursor.getColumnName(i), ignoreCase = true)) {
                        fullPath = cursor.getString(i)
                        break
                    }
                }
            } else {
                document_id = document_id.substring(document_id.lastIndexOf(":") + 1)
                cursor.close()
                val projection = arrayOf(column)
                try {
                    cursor = ctx.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        MediaStore.Images.Media._ID + " = ? ",
                        arrayOf(document_id),
                        null
                    )
                    if (cursor != null) {
                        cursor.moveToFirst()
                        fullPath = cursor.getString(cursor.getColumnIndexOrThrow(column))
                    }
                } finally {
                    cursor.close()
                }
            }
        }
        return fullPath
    }

    //-----------------------------------------------------------------------
    //                             Google Login
    //-----------------------------------------------------------------------
    // 구글 세팅
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
            /* 회원탈퇴
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
        val page = mapOf("page" to 1, "limit" to 1000)
        RetrofitBuild.api.getAccount(accessToken, page).enqueue(object : Callback<ResultAccountList> {
            override fun onResponse(call: Call<ResultAccountList>, response: Response<ResultAccountList>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                    // 받아온 계좌 생성
                    initRecycler(response.body()!!.rows)
                } else { // code == 400
                    val errorResponse: ErrorResult? = gson.fromJson(response.errorBody()!!.charStream(), type)
                    Log.d(TAG2, "연결실패")
                    when(errorResponse!!.code){
                        // access토큰 만료
                        40300 -> {
                            postRefresh(refreshToken)
                            getAccount(bearerAccessToken)
                        }
                    }
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
        RetrofitBuild.api.postAccount(accessToken, account).enqueue(object : Callback<ResultId> {
            override fun onResponse(call: Call<ResultId>, response: Response<ResultId>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                    // 계좌 추가
                    ProfileDataList.datas.apply {
                        add(ProfileData(title = account.title, description = account.description ,id = responseApi!!.id))
                        profileAdapter.datas = ProfileDataList.datas
                        profileAdapter.notifyDataSetChanged()
                    }
                } else { // code == 400
                    val errorResponse: ErrorResult? = gson.fromJson(response.errorBody()!!.charStream(), type)
                    Log.d(TAG2, "연결실패")
                    when(errorResponse!!.code){
                        // access토큰 만료
                        40300 -> {
                            postRefresh(refreshToken)
                            postAccount(bearerAccessToken, account)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<ResultId>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }

    private fun getMyInfo(accessToken: String){
        RetrofitBuild.api.getMyInfo(accessToken).enqueue(object : Callback<ResultMyInfo> {
            override fun onResponse(call: Call<ResultMyInfo>, response: Response<ResultMyInfo>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())

                    userName = responseApi!!.name
                    userEmail = responseApi.email
                    userProfile = responseApi.img_url
                    // 네비뷰 헤더 정보(이메일, 이름) 초기화
                    text_userEmail.text = userEmail
                    text_email.text = userEmail
                    text_name.text = userName
                    et_name.setText(userName)
                    if(userProfile == null){
                        img_profile.setImageResource(R.drawable.profile)
                    }
                    else{
                        val url = "https://checkmoneyproject.azurewebsites.net/api" + userProfile
                        Glide.with(this@MainActivity).load(url).into(img_profile)
                    }

                } else { // code == 400
                    val errorResponse: ErrorResult? = gson.fromJson(response.errorBody()!!.charStream(), type)
                    Log.d(TAG2, "연결실패")
                    when(errorResponse!!.code){
                        // access토큰 만료
                        40300 -> {
                            postRefresh(refreshToken)
                            getMyInfo(bearerAccessToken)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<ResultMyInfo>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }

    private fun putMyInfo(accessToken: String, editMyInfo: EditMyInfo){
        RetrofitBuild.api.putMyInfo(accessToken,editMyInfo).enqueue(object : Callback<Result> {
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                    edit_user_dlg.dismiss()
                } else { // code == 400
                    val errorResponse: ErrorResult? = gson.fromJson(response.errorBody()!!.charStream(), type)
                    Log.d(TAG2, "연결실패")
                    when(errorResponse!!.code){
                        40007 -> {
                            postRefresh(refreshToken)
                            putMyInfo(bearerAccessToken, editMyInfo)
                        }
                        40008 -> text_EditCheck.text = "이전 비밀번호가 일치하지 않습니다."
                        // access토큰 만료
                        40300 -> {
                            postRefresh(refreshToken)
                            putMyInfo(bearerAccessToken, editMyInfo)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<Result>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }

    private fun postImage(accessToken: String, body: MultipartBody.Part){
        RetrofitBuild.api.postImage(accessToken,body).enqueue(object : Callback<ResultImageUrl> {
            override fun onResponse(call: Call<ResultImageUrl>, response: Response<ResultImageUrl>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                    userProfile = responseApi?.url
                    if (editUserPassword == "") {
                        val myInfo = EditMyInfo(userProfile, editUserName, null, null)
                        text_name.text = editUserName
                        putMyInfo(bearerAccessToken, myInfo)
                    } else {
                        val myInfo =
                            EditMyInfo(userProfile, editUserName, editOldPassword, editUserPassword)
                        text_name.text = editUserName
                        putMyInfo(bearerAccessToken, myInfo)
                    }
                } else { // code == 400
                    val errorResponse: ErrorResult? = gson.fromJson(response.errorBody()!!.charStream(), type)
                    Log.d(TAG2, "연결실패")
                    when(errorResponse!!.code){
                        // access토큰 만료
                        40300 -> {
                            postRefresh(refreshToken)
                            postImage(bearerAccessToken, body)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<ResultImageUrl>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }

    private fun postRefresh(refreshToken: RefreshToken){
        RetrofitBuild.api.postRefresh(refreshToken).enqueue(object : Callback<ResultAndToken> {
            override fun onResponse(call: Call<ResultAndToken>, response: Response<ResultAndToken>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                    access_token = responseApi!!.access_token!!
                    bearerAccessToken = "Bearer $access_token"
                } else { // code == 400
                    Log.d(TAG2, "연결실패")
                    //refresh토큰 만료시 로그아웃
                    AppPref.prefs.clearUser(this@MainActivity)
                    googleSignOut()
                    val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(loginIntent)
                    finish()
                }
            }
            override fun onFailure(call: Call<ResultAndToken>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }

    private fun getAllTransAction(accessToken: String){
        val page = mapOf("page" to 1, "limit" to 1000)
        RetrofitBuild.api.getAllTransaction(accessToken, page).enqueue(object : Callback<ResultTransactions> {
            override fun onResponse(call: Call<ResultTransactions>, response: Response<ResultTransactions>) {
                if(response.isSuccessful) { // <--> response.code == 200
                    Log.d(TAG2, "연결성공")
                    val responseApi = response.body()
                    Log.d(TAG2,responseApi.toString())
                    allTransaction = responseApi!!.rows
                    // viewpager 생성
                    renderViewPager()
                    renderTabLayer()
                } else { // code == 400
                    val errorResponse: ErrorResult? = gson.fromJson(response.errorBody()!!.charStream(), type)
                    Log.d(TAG2, "연결실패")
                    when(errorResponse!!.code){
                        // access토큰 만료
                        40300 -> {
                            postRefresh(refreshToken)
                            postImage(bearerAccessToken, body)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<ResultTransactions>, t: Throwable) { // code == 500
                // 실패 처리
                Log.d(TAG2, "인터넷 네트워크 문제")
                Log.d(TAG2, t.toString())
            }
        })
    }

    //-------------------------------------------------------------------------------------
    //                                      Permission
    //-------------------------------------------------------------------------------------

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_PERMISSION_GALLERY){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openGallery()
            } else{
                Toast.makeText(this,"권한이 없어 해당 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun galleryPermissionGranted(dlg: Dialog): Boolean {
        val preference = getPreferences(Context.MODE_PRIVATE)
        val isFirstCheck = preference.getBoolean("isFirstPermissionCheckGallery", true)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    dlg.dismiss()
                // 거부할 경우 왜 필요한지 설명
                val snackBar = Snackbar.make(layoutDrawer, "권한이 필요합니다", Snackbar.LENGTH_INDEFINITE)
                snackBar.setAction("권한승인") {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ), REQ_PERMISSION_GALLERY
                    )
                }
                snackBar.show()
            } else {
                if (isFirstCheck) {
                    // 처음 물었는지 여부를 저장
                    preference.edit().putBoolean("isFirstPermissionCheckGallery", false).apply()
                    // 권한요청
                    ActivityCompat.requestPermissions(this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ), REQ_PERMISSION_GALLERY
                    )
                } else {
                    // 사용자가 권한을 거부하면서 다시 묻지않음 옵션을 선택한 경우
                    // requestPermission을 요청해도 창이 나타나지 않기 때문에 설정창으로 이동한
                    val snackBar = Snackbar.make(layoutDrawer, "권한이 필요합니다 확인을 누르시면 이동합니다", Snackbar.LENGTH_INDEFINITE)
                    snackBar.setAction("확인") {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    snackBar.show()
                }
            }
            return false
        } else {
            return true
        }
    }
}