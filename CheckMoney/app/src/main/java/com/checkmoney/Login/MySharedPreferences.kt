package com.checkmoney

import android.content.Context
import android.content.SharedPreferences

class MySharedPreferences(context: Context) {
    val PREFS_FILENAME = "prefs"
    val PREF_KEY_ID = "id"
    val PREF_KEY_PW = "pw"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)
    /* 파일 이름과 EditText를 저장할 Key 값을 만들고 prefs 인스턴스 초기화 */

    var myId: String?
        get() = prefs.getString(PREF_KEY_ID, "")
        set(value) = prefs.edit().putString(PREF_KEY_ID, value).apply()

    var myPw: String?
        get() = prefs.getString(PREF_KEY_PW, "")
        set(value) = prefs.edit().putString(PREF_KEY_PW, value).apply()

    fun clearUser(context: Context) {
        val prefs : SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()
        editor.clear()
        editor.commit()
    }
}
