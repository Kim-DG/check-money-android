package com.checkmoney

import android.content.Intent
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.Insets.add
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val datas = mutableListOf<ProfileData>()
    private var mBackWait:Long = 0
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var layout_drawer: DrawerLayout
    private lateinit var nav_header: View
    private lateinit var rv_profile: RecyclerView
    private lateinit var btn_navi: ImageView
    private lateinit var btn_logout: TextView
    private lateinit var naviView: NavigationView
    private lateinit var text_email: TextView
    private lateinit var text_logout: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setVariable()
        initRecycler()
        setLayoutSize()

        btn_navi.setOnClickListener {
            layout_drawer.openDrawer(GravityCompat.START)
        }

        text_logout.setOnClickListener {
            //로그아웃 구현
        }

        btn_logout.setOnClickListener {
            AppPref.prefs.clearUser(this)
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()

            naviView.setNavigationItemSelectedListener(this) // 네비게이션 메뉴 아이템에 클릭 속성 부여
        }
    }

    private fun setVariable() {
        layout_drawer = findViewById(R.id.layout_drawer)
        btn_navi = findViewById(R.id.btn_navi)
        naviView = findViewById(R.id.naviView)
        nav_header = naviView.getHeaderView(0)
        rv_profile = nav_header.findViewById(R.id.rv_profile)
        btn_logout = nav_header.findViewById(R.id.text_logout)
        text_email = nav_header.findViewById(R.id.text_email)
        text_logout = nav_header.findViewById(R.id.text_logout)
    }

    private fun setLayoutSize() {
        val display = windowManager.defaultDisplay // in case of Activity
        val size = Point()
        display.getRealSize(size) // or getSize(size)
        val width = size.x * (0.66)
        val height = size.y * (0.66)
        nav_header.layoutParams.height = height.toInt()
        naviView.layoutParams.width= width.toInt()
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean { // 네비게이션 메뉴 아이템 클릭 시 수행
        when (item.itemId) {
            R.id.add -> Toast.makeText(applicationContext, "test1", Toast.LENGTH_SHORT).show()
            R.id.test2 -> Toast.makeText(applicationContext, "test2", Toast.LENGTH_SHORT).show()
            R.id.test3 -> Toast.makeText(applicationContext, "test3", Toast.LENGTH_SHORT).show()
        }
        layout_drawer.closeDrawers() //네비게이션 뷰 닫기
        return false
    }

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

    private fun initRecycler() {
        profileAdapter = ProfileAdapter(this)
        rv_profile.adapter = profileAdapter

        datas.apply {
            add(ProfileData(name = "name"))
            profileAdapter.datas = datas
            profileAdapter.notifyDataSetChanged()
        }
    }
}