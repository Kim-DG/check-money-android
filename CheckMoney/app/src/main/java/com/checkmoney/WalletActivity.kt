package com.checkmoney

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class WalletActivity : AppCompatActivity() {
    private lateinit var datas: ProfileData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        datas = intent.getParcelableExtra("data")!!
    }
}